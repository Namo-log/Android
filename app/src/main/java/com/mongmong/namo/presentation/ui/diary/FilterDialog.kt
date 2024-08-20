package com.mongmong.namo.presentation.ui.diary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.R
import com.mongmong.namo.databinding.DialogFilterBinding
import com.mongmong.namo.presentation.config.FilterState

class FilterDialog(val filter: FilterState?) : DialogFragment() {

    private lateinit var binding: DialogFilterBinding

    // 인터페이스 정의: 선택된 필터를 전달하는 콜백
    interface OnFilterSelectedListener {
        fun onFilterSelected(filter: FilterState)
    }

    private var listener: OnFilterSelectedListener? = null

    fun setOnFilterSelectedListener(listener: OnFilterSelectedListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogFilterBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        // 기존에 선택된 필터 상태를 복원
        binding.filterRadioGroup.check(getCheckedRadioButtonId(filter))

        // 확인 버튼 클릭 시
        binding.filterSaveTv.setOnClickListener {
            val selectedFilter = when (binding.filterRadioGroup.checkedRadioButtonId) {
                R.id.filter_radio_none -> FilterState.NONE
                R.id.filter_radio_title -> FilterState.TITLES
                R.id.filter_radio_content -> FilterState.CONTENTS
                R.id.filter_radio_participant -> FilterState.PARTICIPANTS
                else -> FilterState.NONE
            }
            listener?.onFilterSelected(selectedFilter)
            dismiss() // 다이얼로그 닫기
        }

        // 닫기 버튼 클릭 시
        binding.filterBackTv.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),  // 화면 너비의 90%로 설정
            ViewGroup.LayoutParams.WRAP_CONTENT  // 높이는 WRAP_CONTENT로 유지
        )
    }


    private fun getCheckedRadioButtonId(filterState: FilterState?): Int {
        return when (filterState) {
            FilterState.NONE -> R.id.filter_radio_none
            FilterState.TITLES -> R.id.filter_radio_title
            FilterState.CONTENTS -> R.id.filter_radio_content
            FilterState.PARTICIPANTS -> R.id.filter_radio_participant
            else -> R.id.filter_radio_none
        }
    }
}
