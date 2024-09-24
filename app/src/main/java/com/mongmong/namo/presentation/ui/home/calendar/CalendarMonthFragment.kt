package com.mongmong.namo.presentation.ui.home.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.databinding.FragmentCalendarMonthBinding
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.diary.MoimMemoDetailActivity
import com.mongmong.namo.presentation.ui.diary.PersonalDiaryDetailActivity
import com.mongmong.namo.presentation.ui.home.HomeFragment
import com.mongmong.namo.presentation.ui.home.schedule.adapter.DailyScheduleRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.PersonalScheduleViewModel
import com.mongmong.namo.presentation.utils.CustomCalendarView
import com.mongmong.namo.presentation.utils.PickerConverter
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CalendarMonthFragment : BaseFragment<FragmentCalendarMonthBinding>(R.layout.fragment_calendar_month) {
    private var millis: Long = 0L

    private val personalDailyScheduleAdapter = DailyScheduleRVAdapter()
    private val groupDailyScheduleAdapter = DailyScheduleRVAdapter()

    private val viewModel: PersonalScheduleViewModel by viewModels()


    override fun setup() {
        arguments?.let {
            millis = it.getLong(MILLIS)
        }

        binding.viewModel = viewModel

        binding.calendarMonthView.setDays(millis)
        viewModel.setMonthDayList(binding.calendarMonthView.days)

        initClickListeners()
        initAdapter()
        initObserve()
    }

    override fun onResume() {
        super.onResume()

        getCategoryList()
        setMonthCalendarSchedule()
        setAdapter()
    }

    private fun initClickListeners() {
        // 새 일정 추가
        binding.homeFab.setOnClickListener {
            val intent = Intent(context, ScheduleActivity::class.java)
            intent.putExtra("nowDay", viewModel.getClickedDate().millis)
            requireActivity().startActivity(intent)
        }
        // 캘린더 날짜 클릭
        binding.calendarMonthView.onDateClickListener =
            object : CustomCalendarView.OnDateClickListener {
                override fun onDateClick(date: DateTime?, pos: Int?) {
                    val prevFragment = HomeFragment.currentFragment as CalendarMonthFragment?
                    if (prevFragment != null && prevFragment != this@CalendarMonthFragment) {
                        prevFragment.binding.calendarMonthView.selectedDate = null
                        prevFragment.binding.constraintLayout.transitionToStart()
                    }

                    HomeFragment.currentFragment = this@CalendarMonthFragment
                    HomeFragment.currentSelectedPos = pos
                    HomeFragment.currentSelectedDate = date

                    binding.calendarMonthView.selectedDate = date

                    if (date != null && pos != null) {
                        // 클릭 처리
                        personalDailyScheduleAdapter.setClickedDate(date)
                        groupDailyScheduleAdapter.setClickedDate(date)

                        viewModel.clickDate(pos)
                        setDailySchedule()

                        if (viewModel.isCloseScheduleDetailBottomSheet()) {
                            binding.constraintLayout.transitionToStart()
                            binding.calendarMonthView.selectedDate = null
                            HomeFragment.currentFragment = null
                            HomeFragment.currentSelectedPos = null
                            HomeFragment.currentSelectedDate = null
                        } else if (viewModel.isShow.value == false) { // 바텀시트 닫기
                            binding.constraintLayout.transitionToEnd()
                        }
                        viewModel.updateIsShow()
                    }

                binding.calendarMonthView.invalidate()
                }
            }
    }

    private fun initAdapter() {
        personalDailyScheduleAdapter.initScheduleTimeConverter()
        groupDailyScheduleAdapter.initScheduleTimeConverter()
    }

    private fun setAdapter() {
        /** 개인 */
        binding.homeDailyPersonalScheduleRv.apply {
            adapter = personalDailyScheduleAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        personalDailyScheduleAdapter.setDailyScheduleClickListener(object : DailyScheduleRVAdapter.PersonalScheduleClickListener {
            override fun onContentClicked(schedule: GetMonthScheduleResult) { // 아이템 전체 클릭
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule.convertServerScheduleResponseToLocal())
                requireActivity().startActivity(intent)
            }

            override fun onDiaryIconClicked(schedule: GetMonthScheduleResult, paletteId: Int) { // 기록 아이콘 클릭
                if (schedule.isMeetingSchedule) return
                val intent = Intent(context, PersonalDiaryDetailActivity::class.java)
                    .putExtra("scheduleId", schedule.scheduleId)
                Log.d("CalendarMonthFragment onDiaryIconClicked", "$schedule")
                requireActivity().startActivity(intent)
            }
        })

        /** 모임 */
        binding.homeDailyMoimScheduleRv.apply {
            adapter = groupDailyScheduleAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        groupDailyScheduleAdapter.setDailyScheduleClickListener(object : DailyScheduleRVAdapter.PersonalScheduleClickListener {
            override fun onContentClicked(schedule: GetMonthScheduleResult) { // 아이템 전체 클릭
                requireActivity().startActivity(Intent(context, ScheduleActivity::class.java)
                    .putExtra("schedule", schedule.convertServerScheduleResponseToLocal())
                )
            }

            override fun onDiaryIconClicked(schedule: GetMonthScheduleResult, paletteId: Int) { // 기록 아이콘 클릭
                if (!schedule.isMeetingSchedule) return
                requireActivity().startActivity(
                    Intent(context, MoimMemoDetailActivity::class.java)
                        .putExtra("moimScheduleId", schedule.scheduleId)
                        .putExtra("paletteId", paletteId)
                )
            }
        })
    }

    private fun getCategoryList() {
        viewModel.getCategories()
    }

    private fun setCategoryList(categoryList: List<Category>) {
        binding.calendarMonthView.setCategoryList(categoryList)
        personalDailyScheduleAdapter.setCategoryList(categoryList)
        groupDailyScheduleAdapter.setCategoryList(categoryList)
    }

    // 캘린더에 표시할 월별 일정
    private fun setMonthCalendarSchedule() {
        val dateTime = DateTime(millis)
        Log.d("CalendarMonthFrag", "dateTime: $dateTime")
        viewModel.getMonthSchedules(dateTime.year, dateTime.monthOfYear)
    }

    // 일정 상세보기
    private fun setDailySchedule() {
        binding.dailyScrollSv.scrollTo(0, 0)
        // 일정 아이템 표시
        personalDailyScheduleAdapter.addSchedules(viewModel.getDailySchedules(false))
        groupDailyScheduleAdapter.addSchedules(viewModel.getDailySchedules(true))
    }

    private fun initObserve() {
        // 카테고리 리스트
        viewModel.categoryList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                setCategoryList(it)
            }
        }
        // 일정 리스트
        viewModel.scheduleList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                // 달력의 일정 표시
                drawMonthCalendar(it)
            }
        }
    }

    private fun drawMonthCalendar(scheduleList: List<GetMonthScheduleResult>) {
        binding.calendarMonthView.setScheduleList(scheduleList)
        if (HomeFragment.currentFragment == null) {
            return
        } else if (this@CalendarMonthFragment == HomeFragment.currentFragment) {
            binding.calendarMonthView.selectedDate = HomeFragment.currentSelectedDate
            viewModel.clickDate(HomeFragment.currentSelectedPos!!)
            setDailySchedule()
            binding.constraintLayout.transitionToEnd()
            viewModel.updateIsShow()
        }
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis: Long) = CalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun yearMonthDate(millis: Long): String {
            return SimpleDateFormat("yyyy.MM").format(millis)
        }
    }
}