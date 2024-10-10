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

class ActivityPaymentDialog(private val position: Int) : DialogFragment() {

    lateinit var binding: DialogActivityPaymentBinding
    private lateinit var participantsAdapter: ActivityPaymentsRVAdapter
    private val viewModel: MoimDiaryViewModel by activityViewModels()

    private var totalAmount: Int = 0
    private var selectedParticipantsCount: Int = 0
    private lateinit var payment: ActivityPayment

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogActivityPaymentBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        binding.viewModel = viewModel
        payment = viewModel.activities.value?.get(position)?.payment ?: ActivityPayment(participants = emptyList())
        binding.payment = payment

        totalAmount = payment.totalAmount
        selectedParticipantsCount = payment.participants.count { it.isPayer }

        // EditText에 TextWatcher 설정
        binding.activityPaymentTotalEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                totalAmount = s?.toString()?.toIntOrNull() ?: 0
                updatePerPersonAmount()
            }
        })

        initRecyclerView()

        binding.activityPaymentBackTv.setOnClickListener { dismiss() }
        binding.activityPaymentSaveTv.setOnClickListener {
            savePaymentData()
            dismiss()
        }

        return binding.root
    }

    private fun initRecyclerView() {
        binding.activityParticipantsRv.apply {
            participantsAdapter = ActivityPaymentsRVAdapter(
                participants = payment.participants,
                onCheckedChanged = { updateParticipantsCount() }, // 체크 상태 변경 시 호출
                hasDiary = viewModel.diarySchedule.value?.hasDiary ?: false,
                isEdit = viewModel.isEditMode.value ?: false
            )
            adapter = participantsAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun updateParticipantsCount() {
        selectedParticipantsCount = participantsAdapter.getSelectedParticipantsCount()
        binding.activityPaymentCountTv.text = "$selectedParticipantsCount 명"
        updatePerPersonAmount()
    }

    private fun updatePerPersonAmount() {
        if (selectedParticipantsCount > 0) {
            val amountPerPerson = totalAmount / selectedParticipantsCount
            binding.activityPaymentResultTv.text = "$amountPerPerson 원"
        } else {
            binding.activityPaymentResultTv.text = "0 원"
        }
    }

    private fun savePaymentData() {
        // 선택된 참가자 정보 업데이트
        payment.totalAmount = totalAmount
        payment.amountPerPerson = if (selectedParticipantsCount > 0) totalAmount / selectedParticipantsCount else 0
        payment.divisionCount = selectedParticipantsCount
        payment.participants = participantsAdapter.getUpdatedParticipants()
        viewModel.updateActivityPayment(position, payment)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
