package com.mongmong.namo.presentation.ui.bottom.diary.moimDiary

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
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.data.local.entity.diary.DiaryGroupSchedule
import com.mongmong.namo.databinding.DialogGroupPayBinding
import com.mongmong.namo.domain.model.GroupUser
import com.mongmong.namo.presentation.ui.bottom.diary.moimDiary.adapter.GroupPayMemberRVAdapter
import java.lang.Boolean.TRUE
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class GroupPayDialog(
    private var placeMember: List<GroupUser>,
    private var placeSchedule: DiaryGroupSchedule,
    private val pay: (Long) -> Unit,
    private val checkedMember: (List<Long>) -> Unit
) : DialogFragment(), View.OnClickListener {  // 그룹 다이어리 장소별 정산 다이얼로그

    lateinit var binding: DialogGroupPayBinding
    private lateinit var payMemberRVAdapter: GroupPayMemberRVAdapter

    private var memberIsChecked = mutableListOf<Pair<Long, Boolean>>()
    private var memberCount = 0
    private var totalPay: Long = 0L

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogGroupPayBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        val memberIntList = placeMember.map { it.userId }  // 그룹 멤버의 Id만 가져와서 리스트 만들기

        memberIsChecked.addAll(memberIntList.map { userId ->  // 체크된 그룹 멤버 false로 초기화
            userId to false
        })


        binding.groupPayTotalEt.isSingleLine = true
        binding.groupPayTotalEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val originalText = s.toString()
                if (originalText.isNotEmpty()) {
                    val cleanedText = originalText.replace(",", "")

                    try {
                        val cleanNumber = cleanedText.toLong()
                        val formattedNumber = NumberFormat.getNumberInstance(Locale.US).format(cleanNumber)
                        binding.groupPayTotalEt.removeTextChangedListener(this) // 변경 이벤트 무한 루프 방지
                        binding.groupPayTotalEt.setText(formattedNumber)
                        binding.groupPayTotalEt.setSelection(formattedNumber.length) // 커서를 마지막으로 이동
                        binding.groupPayTotalEt.addTextChangedListener(this)
                    } catch (e: NumberFormatException) {
                        // 숫자로 변환할 수 없는 경우
                    }
                }
                updateResultText()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        if (placeSchedule.pay != 0L) { // 가져온 데이터 할당
            memberCount = placeSchedule.members.size
            totalPay = placeSchedule.pay
            binding.groupPayCountTv.text = memberCount.toString()
            binding.groupPayTotalEt.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPay))
            updateResultText()

            for (i in placeSchedule.members) {
                val index = memberIsChecked.indexOfFirst { it.first == i }
                if (index != -1) {
                    memberIsChecked[index] = i to true // 체크된 그룹 멤버 true로 변경
                }
            }
        }

        account()
        onClickListener()

        return binding.root
    }


    private fun account() {

        payMemberRVAdapter = GroupPayMemberRVAdapter(placeMember, memberIsChecked)
        binding.groupPayPersonRv.apply {
            adapter = payMemberRVAdapter
            layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(TRUE)
        }

        payMemberRVAdapter.setPeopleItemClickListener(object :
            GroupPayMemberRVAdapter.PeopleItemClickListener {
            @SuppressLint("SetTextI18n")
            override fun onItemClick(
                peopleList: ArrayList<String>,
                memberIsChecked: MutableList<Pair<Long, Boolean>>
            ) {

                val checkedPeopleCount = memberIsChecked.count {
                    it.second
                } // 체크된 멤버 수

                memberCount = checkedPeopleCount
                binding.groupPayCountTv.text = memberCount.toString()

                updateResultText()
            }
        })
    }


    private fun updateResultText() {

        val inputText = binding.groupPayTotalEt.text.toString().replace(",", "")   // 구분자 빼고 총 정산 금액 가져 오기
        totalPay = inputText.toLongOrNull() ?: 0L

        if (totalPay.toString().isNotEmpty()) {

            if (memberCount != 0) {
                val result = totalPay / memberCount
                binding.groupPayResultTv.text = NumberFormat.getNumberInstance(Locale.US).format(result)
            } else {
                binding.groupPayResultTv.text = "0"
            }
        } else {
            // 빈 문자열
            binding.groupPayResultTv.text = "0"
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun onClickListener() {

        binding.apply {

            groupPayBackTv.setOnClickListener {
                dismiss()
            }
            groupPaySaveTv.setOnClickListener {

                pay(if (memberCount!=0) totalPay else 0)

                val checkedMemberId = mutableListOf<Long>()  // 체크된 멤버 아이디 리스트

                for (i in memberIsChecked.indices) {
                    if (memberIsChecked[i].second) {
                        val userId = placeMember[i].userId
                        checkedMemberId.add(userId)
                    }
                }
                checkedMember(checkedMemberId)
                dismiss()
            }
        }
    }

    override fun onClick(p0: View?) {
        dismiss()
    }
}
