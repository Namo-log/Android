package com.mongmong.namo.presentation.ui.community.calendar

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.databinding.DialogCalendarInfoBinding
import com.mongmong.namo.presentation.ui.community.calendar.adapter.CalendarScheduleColorInfoRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarInfoDialog : DialogFragment() {
    private val viewModel: CalendarViewModel by activityViewModels()
    private lateinit var binding: DialogCalendarInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCalendarInfoBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initViews()
        setAdapter()
        initClickListeners()
        return binding.root
    }

    private fun initViews() {
        binding.isFriendCalendar = viewModel.isFriendCalendar
    }

    private fun setAdapter() {
        // 친구 캘린더라면 친구의 카테고리 정보, 모임 캘린더라면 참석자의 색상 정보
        val calendarColorInfo = if (viewModel.isFriendCalendar) viewModel.friendCategoryList.map { it.getCategoryColorInfo() } else viewModel.moimSchedule.getParticipantsColoInfo()
        val calendarScheduleColorInfoRVAdapter = CalendarScheduleColorInfoRVAdapter(calendarColorInfo)
        binding.calendarInfoColorRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = calendarScheduleColorInfoRVAdapter
        }
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭 시
        binding.calendarInfoCloseIv.setOnClickListener {
            dismiss()
        }
    }
}
