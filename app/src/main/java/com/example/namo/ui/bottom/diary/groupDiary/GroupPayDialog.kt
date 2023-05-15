package com.example.namo.ui.bottom.diary.groupDiary

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.DialogGroupPayBinding
import com.example.namo.ui.bottom.diary.adapter.GroupPayMemberRVAdapter
import java.lang.Boolean.TRUE


class GroupPayDialog (var placeMember:List<GroupDiaryMember>): DialogFragment(), View.OnClickListener{  // 그룹 다이어리 장소별 정산 다이얼로그

    lateinit var binding: DialogGroupPayBinding
    lateinit var payMemberRVAdapter: GroupPayMemberRVAdapter

    private var peopleCount : Int = 0
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

    private fun account(){

        payMemberRVAdapter= GroupPayMemberRVAdapter(placeMember,memberIsChecked)
        binding.groupPayPersonRv.apply {
            adapter=payMemberRVAdapter
            layoutManager= GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            setHasFixedSize(TRUE)
        }

        payMemberRVAdapter.setPeopleItemClickListener(object :GroupPayMemberRVAdapter.PeopleItemClickListener{
            @SuppressLint("SetTextI18n")
            override fun onItemClick(
                peopleList: ArrayList<GroupDiaryMember>,
                memberIsChecked: ArrayList<Boolean>
            ) {
                for (i in (0 until peopleList.size)){
                    if (memberIsChecked[i]) {
                        peopleCount++
                    }
                }
                binding.groupPayCountTv.text = "$peopleCount 명"

                if (binding.groupPayTotalEt.text.toString() != "금액 입력"){
                    totalPay = binding.groupPayTotalEt.text.toString().toInt()
                    eachPay = totalPay / peopleCount
                    binding.groupPayResultTv.text = "$eachPay"
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

                val sharedPreference = requireActivity().getSharedPreferences("getPay", MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putInt("eachPay",eachPay) //정산 금액
                editor.apply()
                dismiss()
            }
        }
    }

    override fun onClick(p0: View?) {
        dismiss()
    }
}
