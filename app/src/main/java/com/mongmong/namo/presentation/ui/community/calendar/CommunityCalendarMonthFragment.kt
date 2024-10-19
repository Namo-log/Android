package com.mongmong.namo.presentation.ui.community.calendar

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentCommunityCalendarMonthBinding
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity
import com.mongmong.namo.presentation.ui.community.calendar.adapter.ParticipantDailyScheduleRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.adapter.DailyScheduleRVAdapter
import com.mongmong.namo.presentation.utils.CustomCalendarView
import com.mongmong.namo.presentation.utils.ScheduleTimeConverter
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class CommunityCalendarMonthFragment : BaseFragment<FragmentCommunityCalendarMonthBinding>(R.layout.fragment_community_calendar_month) {
    private var millis : Long = 0L

    private val dailyFriendScheduleAdapter = DailyScheduleRVAdapter() // 친구의 일정
    private val dailyParticipantScheduleAdapter = ParticipantDailyScheduleRVAdapter() // 모임 참석자들의 일정

    private val viewModel : CalendarViewModel by activityViewModels()

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

        setMonthCalendarSchedule()
        setAdapter()
    }

    private fun initViews() {
        binding.communityCalendarMonthView.setDays(millis)
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

                    viewModel.onClickCalendarDate(pos)
                    setDailySchedule()

                    if (viewModel.isCloseScheduleDetailBottomSheet()) {
                        binding.communityCalendarMotionLayout.transitionToStart()
                        binding.communityCalendarMonthView.selectedDate = null
                        CommunityCalendarActivity.currentFragment = null
                        CommunityCalendarActivity.currentSelectedPos = null
                        CommunityCalendarActivity.currentSelectedDate = null
                    } else if (!viewModel.isShowDailyBottomSheet) { // 바텀시트 닫기
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

    // 캘린더에 표시할 월별 일정 조회
    private fun setMonthCalendarSchedule() {
        viewModel.setMonthDayList(binding.communityCalendarMonthView.days)
        viewModel.getMoimCalendarSchedules() // 모임 캘린더 일정 조회 API 호출
    }

    // 일정 상세보기
    private fun setDailySchedule() {
        binding.communityCalendarDailyScrollSv.scrollTo(0,0)
        // 일정 아이템 표시
        if (viewModel.isMoimScheduleExist.value == true) {
            setMoimSchedule(viewModel.getDailySchedules(true).first()) // 해당 모임 일정
        }
        dailyFriendScheduleAdapter.addSchedules(arrayListOf()) // 친구 일정
        dailyParticipantScheduleAdapter.addPersonal(viewModel.getDailySchedules(false)) // 모임 참석자 일정
    }

    private fun initObserve() {
        viewModel.moimScheduleList.observe(viewLifecycleOwner) {
            if (it != null) {
                // 달력의 일정 표시
                drawMonthCalendar(it)
            }
        }
    }

    private fun setMoimSchedule(dailySchedule: MoimCalendarSchedule) {
        binding.communityCalendarDailyMoimScheduleTitleTv.text = dailySchedule.title
        binding.communityCalendarDailyMoimScheduleTimeTv.text = ScheduleTimeConverter(viewModel.getClickedDate())
            .getScheduleTimeText(SchedulePeriod(dailySchedule.startDate, dailySchedule.endDate))
    }

    private fun drawMonthCalendar(scheduleList: List<MoimCalendarSchedule>) {
        binding.communityCalendarMonthView.setScheduleList(scheduleList)

        if (CommunityCalendarActivity.currentFragment == null) {
            return
        } else if (this@CommunityCalendarMonthFragment == CommunityCalendarActivity.currentFragment) {
            binding.communityCalendarMonthView.selectedDate = CommunityCalendarActivity.currentSelectedDate
            viewModel.onClickCalendarDate(CommunityCalendarActivity.currentSelectedPos!!)
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