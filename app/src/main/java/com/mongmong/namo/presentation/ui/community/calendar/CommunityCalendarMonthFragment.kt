package com.mongmong.namo.presentation.ui.community.calendar

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentCommunityCalendarMonthBinding
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity
import com.mongmong.namo.presentation.ui.community.calendar.adapter.ParticipantDailyScheduleRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.adapter.DailyScheduleRVAdapter
import com.mongmong.namo.presentation.utils.CustomCalendarView
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class CommunityCalendarMonthFragment : BaseFragment<FragmentCommunityCalendarMonthBinding>(R.layout.fragment_community_calendar_month) {
    private var millis : Long = 0L

    private val dailyFriendScheduleAdapter = DailyScheduleRVAdapter() // 친구의 일정
    private val dailyParticipantScheduleAdapter = ParticipantDailyScheduleRVAdapter() // 모임 참석자들의 일정

    private val viewModel : CalendarViewModel by viewModels()

    override fun setup() {
        arguments?.let {
            millis = it.getLong(MILLIS)
        }

        binding.viewModel = viewModel

        initViews()
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
                binding.communityCalendarMonthView.selectedDate = null
                binding.communityCalendarMotionLayout.transitionToStart()
                binding.communityCalendarMotionLayout.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    private fun initViews() {
        binding.communityCalendarMonthView.setDays(millis)
        viewModel.setMonthDayList(binding.communityCalendarMonthView.days)
    }

    private fun initClickListeners() {
        // 캘린더 날짜 클릭
        binding.communityCalendarMonthView.onDateClickListener = object : CustomCalendarView.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos: Int?) {
                val prevFragment = CommunityCalendarActivity.currentFragment as CommunityCalendarMonthFragment?
                if (prevFragment != null && prevFragment != this@CommunityCalendarMonthFragment) {
                    prevFragment.binding.communityCalendarMonthView.selectedDate = null
                    prevFragment.binding.communityCalendarMotionLayout.transitionToStart()
                }

                CommunityCalendarActivity.currentFragment = this@CommunityCalendarMonthFragment
                CommunityCalendarActivity.currentSelectedPos = pos
                CommunityCalendarActivity.currentSelectedDate = date

                binding.communityCalendarMonthView.selectedDate = date

                if (date != null && pos != null) {
                    // 클릭 처리
                    dailyParticipantScheduleAdapter.setClickedDate(date)
                    dailyFriendScheduleAdapter.setClickedDate(date)

                    viewModel.clickDate(pos)
                    setDailySchedule()

                    if (viewModel.isCloseScheduleDetailBottomSheet()) {
                        binding.communityCalendarMotionLayout.transitionToStart()
                        binding.communityCalendarMonthView.selectedDate = null
                        CommunityCalendarActivity.currentFragment = null
                        CommunityCalendarActivity.currentSelectedPos = null
                        CommunityCalendarActivity.currentSelectedDate = null
                    } else if (!viewModel.isShow) { // 바텀시트 닫기
                        binding.communityCalendarMotionLayout.transitionToEnd()
                    }
                    viewModel.updateIsShow()
                }

                binding.communityCalendarMonthView.invalidate()
            }
        }
    }

    private fun initAdapter() {
        dailyParticipantScheduleAdapter.initScheduleTimeConverter()
        dailyFriendScheduleAdapter.initScheduleTimeConverter()
    }

    private fun setAdapter() {
        binding.communityCalendarFriendDailyScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = dailyFriendScheduleAdapter
        }

        binding.communityCalendarParticipantDailyScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = dailyParticipantScheduleAdapter
        }
    }

    private fun setDailySchedule() {
        binding.communityCalendarDailyScrollSv.scrollTo(0,0)
        // 일정 아이템 표시
        //TODO: 뷰모델의 데이터 넣기
        //TODO: 캘린더 모드에 따라 데이터 및 어댑터 분리
        dailyFriendScheduleAdapter.addSchedules(arrayListOf())
        dailyParticipantScheduleAdapter.addPersonal(arrayListOf())
    }

    private fun initObserve() {
        // TODO: 뷰모델의 일정을 관측해서 어댑터와 연결
    }

    private fun drawMonthCalendar(scheduleList: ArrayList<MoimScheduleBody>) {
        binding.communityCalendarMonthView.setScheduleList(scheduleList)
        binding.communityCalendarMonthView.invalidate()

        if (CommunityCalendarActivity.currentFragment == null) {
            return
        } else if (this@CommunityCalendarMonthFragment == CommunityCalendarActivity.currentFragment) {
            binding.communityCalendarMonthView.selectedDate = CommunityCalendarActivity.currentSelectedDate
            viewModel.clickDate(CommunityCalendarActivity.currentSelectedPos!!)
            setDailySchedule()
            binding.communityCalendarMotionLayout.transitionToEnd()
            viewModel.updateIsShow()
        }
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = CommunityCalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}