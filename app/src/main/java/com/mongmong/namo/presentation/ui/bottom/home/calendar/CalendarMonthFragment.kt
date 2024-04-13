package com.mongmong.namo.presentation.ui.bottom.home.calendar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.remote.diary.DiaryService
import com.mongmong.namo.data.remote.diary.GetGroupMonthView
import com.mongmong.namo.databinding.FragmentCalendarMonthBinding
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.ui.bottom.diary.moimDiary.MoimMemoDetailActivity
import com.mongmong.namo.presentation.ui.bottom.diary.personalDiary.PersonalDetailActivity
import com.mongmong.namo.presentation.ui.bottom.home.HomeFragment
import com.mongmong.namo.presentation.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.mongmong.namo.presentation.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.mongmong.namo.presentation.ui.bottom.home.schedule.MoimScheduleViewModel
import com.mongmong.namo.presentation.ui.bottom.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.bottom.home.schedule.PersonalScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CalendarMonthFragment : Fragment(), GetGroupMonthView {

    private lateinit var binding: FragmentCalendarMonthBinding

    private var millis: Long = 0L
    var isShow = false
    private lateinit var monthDayList: List<DateTime>
    private var calendarSchedules: ArrayList<Schedule> = arrayListOf() // 캘린더 일정 표시
    private var monthGroupSchedule: ArrayList<Schedule> = arrayListOf()

    private var prevIdx = -1
    private var nowIdx = 0
    private var schedulePersonal: ArrayList<Schedule> = arrayListOf()
    private var scheduleMoim: ArrayList<Schedule> = arrayListOf()
    private val personalScheduleRVAdapter = DailyPersonalRVAdapter()
    private val groupScheduleRVAdapter = DailyGroupRVAdapter()

    private val personalViewModel : PersonalScheduleViewModel by viewModels()
    private val moimViewModel : MoimScheduleViewModel by viewModels()

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

        binding.calendarMonthView.setDayList(millis)
        monthDayList = binding.calendarMonthView.getDayList()

        initObserve()
        initClickListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        getCategoryList()
        setMonthCalendarSchedule(monthDayList[0].withTimeAtStartOfDay().millis / 1000, monthDayList[41].plusDays(1).withTimeAtStartOfDay().millis / 1000)
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

    override fun onPause() {
        super.onPause()

        calendarSchedules.clear()
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

//                binding.calendarMonthView.invalidate()
                }
            }
    }

    private fun setAdapter() {
        /** 개인 */
        binding.homeDailyPersonalScheduleRv.apply {
            adapter = personalScheduleRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        personalScheduleRVAdapter.setContentClickListener(object :
            DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(schedule: Schedule) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule)
                requireActivity().startActivity(intent)
            }
        })

        // 기록 아이템 클릭 리스너
        personalScheduleRVAdapter.setRecordClickListener(object :
            DailyPersonalRVAdapter.DiaryInterface {
            override fun onDetailClicked(schedule: Schedule) {

                val intent = Intent(context, PersonalDetailActivity::class.java)
                intent.putExtra("schedule", schedule)
                requireActivity().startActivity(intent)
            }
        })


        /** 모임 */
        binding.homeDailyMoimScheduleRv.apply {
            adapter = groupScheduleRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
//        if (nowIdx==0) setToday()

        groupScheduleRVAdapter.setGorupContentClickListener(object :
            DailyGroupRVAdapter.GroupContentClickListener {
            override fun onGroupContentClick(schedule: Schedule) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule)
                requireActivity().startActivity(intent)
            }
        })

        // 기록
        groupScheduleRVAdapter.setRecordClickListener(object : DailyGroupRVAdapter.DiaryInterface {
            override fun onGroupDetailClicked(monthDiary: MoimDiary?) {

                val intent = Intent(context, MoimMemoDetailActivity::class.java)
                intent.putExtra("groupDiary", monthDiary)
                requireActivity().startActivity(intent)
            }
        })
    }

    private fun getCategoryList() {
        lifecycleScope.launch{
            personalViewModel.getCategories()
        }
    }

    private fun setCategoryList(categoryList: List<Category>) {
        Log.d("CalendarMonthFrag", "categoryList: $categoryList")
        binding.calendarMonthView.setCategoryList(categoryList)
        personalScheduleRVAdapter.setCategory(categoryList)
        groupScheduleRVAdapter.setCategory(categoryList)
    }

    // 캘린더에 표시할 월별 일정
    private fun setMonthCalendarSchedule(monthStart: Long, monthEnd: Long) {
        lifecycleScope.launch {
            personalViewModel.getMonthSchedules(monthStart, monthEnd)
            moimViewModel.getMonthMoimSchedule(yearMonthDate(millis))
        }
    }

    // 일정 상세보기
    private fun setDaily(dateId: Int) {
        binding.homeDailyHeaderTv.text = monthDayList[dateId].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0, 0)
        // 일정 아이템 표시
        getMoimDiary() // 다이어리
        getSchedule(dateId) // 일정 내용
        Log.d("CHECK_GROUP_EVENT", monthGroupSchedule.toString())
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
        lifecycleScope.launch {
            personalViewModel.getDailySchedules(todayStart, todayEnd)
        }
    }

    private fun setDailyMoimSchedule(todayStart: Long, todayEnd: Long) {
        // 선택한 날짜의 모임 일정
        scheduleMoim = monthGroupSchedule.filter { item -> item.startLong <= todayEnd && item.endLong >= todayStart } as ArrayList<Schedule>
        groupScheduleRVAdapter.addGroup(scheduleMoim)
        setMoimEmptyText(scheduleMoim.isEmpty())
    }

    private fun getMoimDiary() {
        try {
            val service = DiaryService()
            service.getGroupMonthDiary2(yearMonthDate(millis), 0, 50)
            service.getGroupMonthView(this@CalendarMonthFragment)
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "Exception occurred: ${e.message}")
        }
    }

    private fun initObserve() {
        // 카테고리 리스트
        personalViewModel.categoryList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                setCategoryList(it)
            }
        }
        // 개인 일정 리스트
        personalViewModel.personalScheduleList.observe(viewLifecycleOwner) {
            schedulePersonal.clear()
            if (!it.isNullOrEmpty()) {
                val dailySchedule = it as ArrayList<Schedule>
                schedulePersonal = dailySchedule.filter { item -> !item.moimSchedule } as ArrayList<Schedule>
            }
            personalScheduleRVAdapter.addPersonal(schedulePersonal)
            Log.d("getDailySchedules", "Personal Schedule : $schedulePersonal")
            setPersonalEmptyText(schedulePersonal.isEmpty())
        }
        personalViewModel.scheduleList.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                calendarSchedules.addAll(it)
            }
        }
        // 모임 일정 리스트
        moimViewModel.scheduleList.observe(viewLifecycleOwner) { result ->
            scheduleMoim.clear()
            if (!result.isNullOrEmpty()) {
                monthGroupSchedule = result.map { it.convertServerScheduleResponseToLocal() } as ArrayList
                calendarSchedules.addAll(monthGroupSchedule)
                binding.calendarMonthView.setScheduleList(calendarSchedules) // 달력 표시
            }
        }
    }

    override fun onGetGroupMonthSuccess(response: DiaryGetMonthResponse) {
        val data = response.result
        groupScheduleRVAdapter.addGroupDiary(data.content as ArrayList<MoimDiary>)
    }

    override fun onGetGroupMonthFailure(message: String) {
        Log.d("GET_GROUP_MONTH", message)
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