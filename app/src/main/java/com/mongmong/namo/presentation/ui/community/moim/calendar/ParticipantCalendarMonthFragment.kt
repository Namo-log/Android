package com.mongmong.namo.presentation.ui.community.moim.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.FragmentMoimParticipantCalendarMonthBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimParticipantCalendarActivity
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleActivity
import com.mongmong.namo.presentation.ui.community.moim.calendar.adapter.GroupDailyMoimRVAdapter
import com.mongmong.namo.presentation.ui.community.moim.calendar.adapter.GroupDailyPersonalRVAdapter
import com.mongmong.namo.presentation.ui.community.moim.schedule.MoimScheduleViewModel
import com.mongmong.namo.presentation.utils.CustomCalendarView
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class ParticipantCalendarMonthFragment : BaseFragment<FragmentMoimParticipantCalendarMonthBinding>(R.layout.fragment_moim_participant_calendar_month) {
    private var millis : Long = 0L

    private val personalDailyScheduleAdapter = GroupDailyPersonalRVAdapter()
    private val groupDailyScheduleAdapter = GroupDailyMoimRVAdapter()

    private val viewModel : MoimScheduleViewModel by viewModels()

    override fun setup() {
        arguments?.let {
            millis = it.getLong(MILLIS)
        }

        binding.viewModel = viewModel

        binding.moimParticipantCalendarMonthView.setDays(millis)
        viewModel.setMonthDayList(binding.moimParticipantCalendarMonthView.days)

        initClickListeners()
        initObserve()
        initAdapter()
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    override fun onPause() {
        super.onPause()
        val listener = object : CustomCalendarView.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos : Int?) {
                binding.moimParticipantCalendarMonthView.selectedDate = null
                binding.constraintLayout2.transitionToStart()
                viewModel.setIsShow(false)
                binding.constraintLayout2.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    private fun initClickListeners() {

        // 날짜 상세보기
        binding.moimParticipantCalendarMonthView.onDateClickListener = object : CustomCalendarView.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos: Int?) {
                val prevFragment = MoimParticipantCalendarActivity.currentFragment as ParticipantCalendarMonthFragment?
                if (prevFragment != null && prevFragment != this@ParticipantCalendarMonthFragment) {
                    prevFragment.binding.moimParticipantCalendarMonthView.selectedDate = null
                    prevFragment.binding.constraintLayout2.transitionToStart()
                }

                MoimParticipantCalendarActivity.currentFragment = this@ParticipantCalendarMonthFragment
                MoimParticipantCalendarActivity.currentSelectedPos = pos
                MoimParticipantCalendarActivity.currentSelectedDate = date

                binding.moimParticipantCalendarMonthView.selectedDate = date

                if (date != null && pos != null) {
                    // 클릭 처리
                    personalDailyScheduleAdapter.setClickedDate(date)
                    groupDailyScheduleAdapter.setClickedDate(date)

                    viewModel.clickDate(pos)
                    setDailySchedule()

                    if (viewModel.isCloseScheduleDetailBottomSheet()) {
                        binding.constraintLayout2.transitionToStart()
                        binding.moimParticipantCalendarMonthView.selectedDate = null
                        MoimParticipantCalendarActivity.currentFragment = null
                        MoimParticipantCalendarActivity.currentSelectedPos = null
                        MoimParticipantCalendarActivity.currentSelectedDate = null
                    } else if (viewModel.isShow.value == false) { // 바텀시트 닫기
                        binding.constraintLayout2.transitionToEnd()
                    }
                    viewModel.updateIsShow()
                }

                binding.moimParticipantCalendarMonthView.invalidate()
            }
        }
    }

    private fun initAdapter() {
        personalDailyScheduleAdapter.initScheduleTimeConverter()
        groupDailyScheduleAdapter.initScheduleTimeConverter()
    }

    private fun setAdapter() {
        binding.groupDailyPersonalScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = personalDailyScheduleAdapter
        }

        binding.groupDailyMoimScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = groupDailyScheduleAdapter
        }
    }

    private fun setDailySchedule() {
        binding.groupScrollSv.scrollTo(0,0)
        // 일정 아이템 표시
        personalDailyScheduleAdapter.addPersonal(viewModel.getDailySchedules(false))
        groupDailyScheduleAdapter.addGroupSchedule(viewModel.getDailySchedules(true))
    }

    private fun initObserve() {
//        viewModel.groupScheduleList.observe(viewLifecycleOwner) { result ->
//            viewModel.filterMonthSchedule()
//            if (!result.isNullOrEmpty()) {
//                drawMonthCalendar(viewModel.monthScheduleList.value!! as ArrayList<MoimScheduleBody>)
//            }
//        }
    }

    private fun drawMonthCalendar(scheduleList: ArrayList<MoimScheduleBody>) {
        binding.moimParticipantCalendarMonthView.setScheduleList(scheduleList)
        binding.moimParticipantCalendarMonthView.invalidate()

        if (MoimParticipantCalendarActivity.currentFragment == null) {
            return
        } else if (this@ParticipantCalendarMonthFragment == MoimParticipantCalendarActivity.currentFragment) {
            binding.moimParticipantCalendarMonthView.selectedDate = MoimParticipantCalendarActivity.currentSelectedDate
            viewModel.clickDate(MoimParticipantCalendarActivity.currentSelectedPos!!)
            setDailySchedule()
            binding.constraintLayout2.transitionToEnd()
            viewModel.updateIsShow()
        }
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = ParticipantCalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}