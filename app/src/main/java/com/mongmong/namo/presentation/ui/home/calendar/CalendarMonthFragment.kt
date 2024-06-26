package com.mongmong.namo.presentation.ui.home.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.databinding.FragmentCalendarMonthBinding
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.presentation.ui.diary.MoimMemoDetailActivity
import com.mongmong.namo.presentation.ui.diary.PersonalDetailActivity
import com.mongmong.namo.presentation.ui.home.HomeFragment
import com.mongmong.namo.presentation.ui.home.adapter.DailyMoimRVAdapter
import com.mongmong.namo.presentation.ui.home.adapter.DailyPersonalRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.home.schedule.PersonalScheduleViewModel
import com.mongmong.namo.presentation.utils.CustomCalendarView
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CalendarMonthFragment : Fragment() {

    private lateinit var binding: FragmentCalendarMonthBinding

    private var millis: Long = 0L
    var isShow = false
    private lateinit var monthDayList: List<DateTime>
    private var calendarSchedules: ArrayList<GetMonthScheduleResult> = arrayListOf() // 캘린더 일정 표시

    private var prevIdx = -1
    private var nowIdx = 0
    private var schedulePersonal: ArrayList<GetMonthScheduleResult> = arrayListOf()
    private var scheduleMoim: ArrayList<GetMonthScheduleResult> = arrayListOf()
    private val personalScheduleRVAdapter = DailyPersonalRVAdapter()
    private val groupScheduleRVAdapter = DailyMoimRVAdapter()

    private val viewModel : PersonalScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            millis = it.getLong(MILLIS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarMonthBinding.inflate(inflater, container, false)

        binding.calendarMonthView.setDays(millis)
        monthDayList = binding.calendarMonthView.days

        initClickListeners()
        initAdapter()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        initObserve()
        getCategoryList()
        setMonthCalendarSchedule()
        setAdapter()

        if (HomeFragment.currentFragment == null) {
            return
        } else if (this@CalendarMonthFragment != HomeFragment.currentFragment) {
            isShow = false
            prevIdx = -1
        } else {
            binding.calendarMonthView.selectedDate = HomeFragment.currentSelectedDate
            nowIdx = HomeFragment.currentSelectedPos!!
            setDaily(nowIdx)
            binding.constraintLayout.transitionToEnd()
            isShow = true
            prevIdx = nowIdx
        }
    }

    private fun initClickListeners() {
        // 새 일정 추가
        binding.homeFab.setOnClickListener {
            val intent = Intent(context, ScheduleActivity::class.java)
            intent.putExtra("nowDay", monthDayList[nowIdx].millis)
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
                        personalScheduleRVAdapter.setClickedDate(date)
                        groupScheduleRVAdapter.setClickedDate(date)

                        nowIdx = pos
                        setDaily(nowIdx)

                        if (isShow && prevIdx == nowIdx) {
                            binding.constraintLayout.transitionToStart()
                            binding.calendarMonthView.selectedDate = null
                            HomeFragment.currentFragment = null
                            HomeFragment.currentSelectedPos = null
                            HomeFragment.currentSelectedDate = null
                        } else if (!isShow) {
                            binding.constraintLayout.transitionToEnd()
                        }
                        isShow = !isShow
                        prevIdx = nowIdx
                    }

                binding.calendarMonthView.invalidate()
                }
            }
    }

    private fun initAdapter() {
        personalScheduleRVAdapter.initScheduleTimeConverter()
        groupScheduleRVAdapter.initScheduleTimeConverter()
    }

    private fun setAdapter() {
        /** 개인 */
        binding.homeDailyPersonalScheduleRv.apply {
            adapter = personalScheduleRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        personalScheduleRVAdapter.setPersonalScheduleClickListener(object : DailyPersonalRVAdapter.PersonalScheduleClickListener {
            override fun onContentClicked(schedule: GetMonthScheduleResult) { // 아이템 전체 클릭
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule.convertServerScheduleResponseToLocal())
                requireActivity().startActivity(intent)
            }

            override fun onDiaryIconClicked(schedule: GetMonthScheduleResult) { // 기록 아이콘 클릭
                val intent = Intent(context, PersonalDetailActivity::class.java)
                intent.putExtra("schedule", schedule.convertServerScheduleResponseToLocal())
                Log.d("CalendarMonthFragment onDiaryIconClicked", "$schedule")
                requireActivity().startActivity(intent)
            }
        })

        /** 모임 */
        binding.homeDailyMoimScheduleRv.apply {
            adapter = groupScheduleRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
        groupScheduleRVAdapter.setMoimScheduleClickListener(object : DailyMoimRVAdapter.MoimScheduleClickListener {
            override fun onContentClicked(schedule: GetMonthScheduleResult) { // 아이템 전체 클릭
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule.convertServerScheduleResponseToLocal())
                requireActivity().startActivity(intent)
            }

            override fun onDiaryIconClicked(scheduleId: Long) { // 기록 아이콘 클릭
                requireActivity().startActivity(
                    Intent(context, MoimMemoDetailActivity::class.java)
                        .putExtra("moimScheduleId", scheduleId)
                )
            }
        })
    }

    private fun getCategoryList() {
        viewModel.getCategories()
    }

    private fun setCategoryList(categoryList: List<Category>) {
//        Log.d("CalendarMonthFrag", "categoryList: $categoryList")
        binding.calendarMonthView.setCategoryList(categoryList)
        personalScheduleRVAdapter.setCategory(categoryList)
        groupScheduleRVAdapter.setCategory(categoryList)
    }

    // 캘린더에 표시할 월별 일정
    private fun setMonthCalendarSchedule() {
        viewModel.getMonthSchedules(yearMonthDate(millis))
    }

    // 일정 상세보기
    private fun setDaily(dateId: Int) {
        binding.homeDailyHeaderTv.text = monthDayList[dateId].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0, 0)
        // 일정 아이템 표시
        getSchedule(dateId) // 일정 내용
    }

    private fun setPersonalEmptyText(isEmpty: Boolean) {
        binding.homeDailyPersonalScheduleNoneTv.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun setMoimEmptyText(isEmpty: Boolean) {
        binding.homeDailyMoimScheduleNoneTv.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun getSchedule(dateId: Int) {
        val todayStart = (monthDayList[dateId].withTimeAtStartOfDay().millis) / 1000
        val todayEnd = (monthDayList[dateId].plusDays(1).withTimeAtStartOfDay().millis - 1) / 1000

        schedulePersonal.clear()
        scheduleMoim.clear()
        setDailyPersonalSchedule(todayStart, todayEnd) // 개인 일정 표시
        setDailyMoimSchedule(todayStart, todayEnd) // 그룹 일정 표시
    }

    private fun setDailyPersonalSchedule(todayStart: Long, todayEnd: Long) {
        // 선택한 날짜의 개인 일정
//        viewModel.getDailySchedules(todayStart, todayEnd)
        schedulePersonal = calendarSchedules.filter { item ->
            item.startDate <= todayEnd && item.endDate >= todayStart
                    && !item.moimSchedule
        } as ArrayList<GetMonthScheduleResult>
        personalScheduleRVAdapter.addPersonal(schedulePersonal)
        setPersonalEmptyText(schedulePersonal.isEmpty())
    }

    private fun setDailyMoimSchedule(todayStart: Long, todayEnd: Long) {
        // 선택한 날짜의 모임 일정
        scheduleMoim = calendarSchedules.filter { item ->
            item.startDate <= todayEnd && item.endDate >= todayStart
                    && item.moimSchedule
        } as ArrayList<GetMonthScheduleResult>
        groupScheduleRVAdapter.addGroup(scheduleMoim)
        setMoimEmptyText(scheduleMoim.isEmpty())
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
            calendarSchedules.clear()
            if (!it.isNullOrEmpty()) {
                calendarSchedules.addAll(it)
            }
            getSchedule(nowIdx)
            binding.calendarMonthView.setScheduleList(calendarSchedules) // 달력 표시
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
            return SimpleDateFormat("yyyy,MM").format(millis)
        }
    }
}