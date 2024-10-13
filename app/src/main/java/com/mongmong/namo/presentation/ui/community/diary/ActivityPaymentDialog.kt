package com.mongmong.namo.presentation.ui.community.diary

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.databinding.DialogActivityPaymentBinding
import com.mongmong.namo.domain.model.ActivityPayment
import com.mongmong.namo.presentation.ui.community.diary.adapter.ActivityPaymentsRVAdapter
import java.text.NumberFormat

class ActivityPaymentDialog(private val position: Int) : DialogFragment() {
    lateinit var binding: DialogActivityPaymentBinding
    private lateinit var participantsAdapter: ActivityPaymentsRVAdapter
    private val viewModel: MoimDiaryViewModel by activityViewModels()

    // 편집 중인 데이터를 저장할 복사본
    private var tempPayment: ActivityPayment? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogActivityPaymentBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        viewModel.activities.value?.get(position)?.let { activity ->
            tempPayment = activity.payment.copy(participants = activity.payment.participants.map { it.copy() })
        } ?: run {
            tempPayment = ActivityPayment(participants = emptyList())  // 기본값
        }
        binding.payment = tempPayment

        // TextWatcher 설정
        binding.activityPaymentTotalEt.addTextChangedListener(object : TextWatcher {
            private var currentText: String = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == currentText) return

                s?.let {
                    val cleanString = it.toString().replace("[^\\d]".toRegex(), "")
                    val parsed = cleanString.toLongOrNull() ?: 0L

                    if (parsed == 0L) {
                        currentText = ""
                        binding.activityPaymentTotalEt.setText("")
                        binding.activityPaymentTotalEt.setSelection(0)
                        tempPayment?.totalAmount = 0
                        binding.activityPaymentResultTv.text = "0 원"
                    } else {
                        val formatted = formatAmount(parsed)
                        currentText = formatted

                        binding.activityPaymentTotalEt.removeTextChangedListener(this)
                        binding.activityPaymentTotalEt.setText(formatted)
                        binding.activityPaymentTotalEt.setSelection(formatted.indexOf(" 원"))
                        binding.activityPaymentTotalEt.addTextChangedListener(this)

                        tempPayment?.totalAmount = parsed.toInt() // totalAmount 대신 tempPayment에 저장
                        updatePerPersonAmount()
                    }
                }
            }
        })

        initRecyclerView()

        binding.activityPaymentBackTv.setOnClickListener { dismiss() }
        binding.activityPaymentSaveTv.setOnClickListener { savePaymentData() }

        return binding.root
    }

    // RecyclerView 초기화
    private fun initRecyclerView() {
        participantsAdapter = ActivityPaymentsRVAdapter(
            participants = tempPayment?.participants ?: emptyList(),
            onCheckedChanged = { updateParticipantsCount() },
            hasDiary = viewModel.diarySchedule.value?.hasDiary ?: false,
            isEdit = viewModel.isEditMode.value ?: false
        )

        binding.activityParticipantsRv.apply {
            adapter = participantsAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    // 참가자 수 업데이트
    private fun updateParticipantsCount() {
        tempPayment?.divisionCount = participantsAdapter.getSelectedParticipantsCount()
        binding.activityPaymentCountTv.text = "${tempPayment?.divisionCount} 명"
        updatePerPersonAmount()
    }

    // 금액 포맷팅
    private fun formatAmount(amount: Long): String {
        return NumberFormat.getInstance().format(amount) + " 원"
    }

    // 인당 금액 업데이트
    private fun updatePerPersonAmount() {
        val selectedCount = tempPayment?.divisionCount ?: 0
        val totalAmount = tempPayment?.totalAmount ?: 0

        if (selectedCount > 0) {
            val amountPerPerson = totalAmount / selectedCount
            binding.activityPaymentResultTv.text = formatAmount(amountPerPerson.toLong())
            tempPayment?.amountPerPerson = amountPerPerson
        } else {
            binding.activityPaymentResultTv.text = "0 원"
            tempPayment?.amountPerPerson = 0
        }
    }

    private fun savePaymentData() {
        tempPayment?.let {
            it.participants = participantsAdapter.getUpdatedParticipants()

            val activityId = viewModel.activities.value?.get(position)?.activityId

            if (activityId == 0L) {
                viewModel.updateActivityPayment(position, it)
            } else {
                viewModel.editActivityPayment(activityId!!, it)
            }
        }
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
