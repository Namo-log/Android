package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.MODE_PRIVATE
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
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.DialogGroupPayBinding
import com.example.namo.ui.bottom.diary.groupDiary.adapter.GroupPayMemberRVAdapter
import java.lang.Boolean.TRUE


class GroupPayDialog(
    private var placeMember:List<GroupDiaryMember>
): DialogFragment(), View.OnClickListener{  // 그룹 다이어리 장소별 정산 다이얼로그

    lateinit var binding: DialogGroupPayBinding
    lateinit var payMemberRVAdapter: GroupPayMemberRVAdapter

    private var totalPay : Int = 0
    private var eachPay : Int = 0
    private var memberIsChecked : ArrayList<Boolean> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogGroupPayBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  //배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  //dialog 모서리 둥글게

        onClickListener()
        account()

        memberIsChecked.apply {
            for (i in (placeMember.indices)) {
                add(false)
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

        payMemberRVAdapter.setPeopleItemClickListener(object : GroupPayMemberRVAdapter.PeopleItemClickListener{
            @SuppressLint("SetTextI18n")
            override fun onItemClick(
                peopleList: ArrayList<GroupDiaryMember>,
                memberIsChecked: ArrayList<Boolean>
            ) {
                val checkedPeopleCount = memberIsChecked.count { it.and(TRUE) }
                binding.groupPayCountTv.text = "$checkedPeopleCount  명"
                // 체크된 멤버 수 계산
                Log.d("memberClick", "$checkedPeopleCount")


                if (binding.groupPayTotalEt.text.isNotEmpty() ){   // 총 금액을 입력했을 때 계산
                    totalPay = binding.groupPayTotalEt.text.toString().toInt()
                    if (checkedPeopleCount!=0){
                        eachPay = totalPay / checkedPeopleCount
                    }else{
                        Toast.makeText(requireContext(), "멤버를 선택해주세요", Toast.LENGTH_SHORT).show()
                    }
                    binding.groupPayResultTv.text = "$eachPay  원"

                }else{ // 총 금액 입력 안하면 메세지
                    Toast.makeText(requireContext(), "금액을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun onClickListener(){

        binding.apply {

            groupPayBackTv.setOnClickListener {
                dismiss()
            }
            groupPaySaveTv.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onClick(p0: View?) {
        dismiss()
    }
}
