package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.databinding.DialogGroupPayBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPayMemberRVAdapter
import java.lang.Boolean.TRUE


class GroupPayDialog(
    private var placeMember: List<DiaryResponse.GroupUser>,
    private var placeEvent: DiaryGroupEvent,
    private val pay: (Int) -> Unit,
    private val checkedMember: (List<Int>) -> Unit
) : DialogFragment(), View.OnClickListener {  // 그룹 다이어리 장소별 정산 다이얼로그

    lateinit var binding: DialogGroupPayBinding
    private lateinit var payMemberRVAdapter: GroupPayMemberRVAdapter

    private var totalPay: Int = 0
    private var eachPay: Int = 0
    private var memberIsChecked = mutableListOf<Pair<Int, Boolean>>()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogGroupPayBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        val memberIntList = placeMember.map { it.userId }

        memberIsChecked.addAll(memberIntList.map { userId ->
            userId to false
        })

        account()
        onClickListener()

        if (placeEvent.pay != 0) {
            val memberSize = placeEvent.members.size
            binding.groupPayTotalEt.setText("${placeEvent.pay * memberSize}")
            binding.groupPayCountTv.text = "$memberSize  명"
            binding.groupPayResultTv.text = "${placeEvent.pay}  원"

            for (i in placeEvent.members) {
                val index = memberIsChecked.indexOfFirst { it.first == i }
                if (index != -1) {
                    memberIsChecked[index] = i to true
                }
            }
        }

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
                memberIsChecked: MutableList<Pair<Int, Boolean>>
            ) {

                val checkedPeopleCount = memberIsChecked.count {
                    it.second
                }
                binding.groupPayCountTv.text = "$checkedPeopleCount  명"
                // 체크된 멤버 수 계산

                val totalText = binding.groupPayTotalEt.text.toString()
                binding.groupPayTotalEt.isSingleLine = true

                if (totalText.contains(Regex("[a-zA-Z]"))) {
                    binding.groupPayTotalEt.text.clear()
                    Toast.makeText(requireContext(), "숫자만 입력 가능합니다!", Toast.LENGTH_SHORT).show()
                }

                if (totalText.isNotEmpty() and (totalText.toIntOrNull() != null)) {   // 총 금액을 입력했을 때 계산
                    totalPay = binding.groupPayTotalEt.text.toString().toLong().toInt()
                    if (checkedPeopleCount != 0) {
                        eachPay = totalPay / checkedPeopleCount
                    } else {
                        Toast.makeText(requireContext(), "멤버를 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                    binding.groupPayResultTv.text = "$eachPay  원"

                } else { // 총 금액 입력 안하면 메세지
                    Toast.makeText(requireContext(), "금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun onClickListener() {

        binding.apply {

            groupPayBackTv.setOnClickListener {
                dismiss()
            }
            groupPaySaveTv.setOnClickListener {

                if (eachPay == 0) {
                    eachPay =
                        binding.groupPayResultTv.text.toString().replace("  원", "").trim().toInt()
                }
                pay(eachPay)

                val checkedMemberId = mutableListOf<Int>()

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
