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
import com.mongmong.namo.presentation.ui.MainActivity.Companion.setCategoryList
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.remote.diary.DiaryService
import com.mongmong.namo.data.remote.diary.GetGroupMonthView
import com.mongmong.namo.data.remote.schedule.ScheduleService
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.data.remote.schedule.GetMonthMoimScheduleView
import com.mongmong.namo.databinding.FragmentCalendarMonthBinding
import com.mongmong.namo.domain.model.DiaryGetMonthResponse

import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.ui.bottom.diary.moimDiary.MoimMemoDetailActivity
import com.mongmong.namo.presentation.ui.bottom.diary.personalDiary.PersonalDetailActivity
import com.mongmong.namo.presentation.ui.bottom.home.HomeFragment
import com.mongmong.namo.presentation.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.mongmong.namo.presentation.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.mongmong.namo.presentation.ui.bottom.home.schedule.ScheduleActivity
import com.mongmong.namo.presentation.ui.bottom.home.schedule.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.text.SimpleDateFormat

@AndroidEntryPoint
class CalendarMonthFragment : Fragment(), GetGroupMonthView, GetMonthMoimScheduleView {

    lateinit var db: NamoDatabase
    private lateinit var binding: FragmentCalendarMonthBinding
    private lateinit var categoryList: List<Category>

    private var millis: Long = 0L
    var isShow = false
    private lateinit var monthList: List<DateTime>
    private lateinit var tempSchedule: ArrayList<Schedule>
    private var monthGroupSchedule: ArrayList<Schedule> = arrayListOf()

    private var prevIdx = -1
    private var nowIdx = 0
    private var schedulePersonal: ArrayList<Schedule> = arrayListOf()
    private var scheduleMoim: ArrayList<Schedule> = arrayListOf()
    private val personalScheduleRVAdapter = DailyPersonalRVAdapter()
    private val groupScheduleRVAdapter = DailyGroupRVAdapter()

    private val viewModel : ScheduleViewModel by viewModels()

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
        db = NamoDatabase.getInstance(requireContext())

        binding.calendarMonthView.setDayList(millis)
        monthList = binding.calendarMonthView.getDayList()

        initObserve()

        binding.homeFab.setOnClickListener {
            val intent = Intent(context, ScheduleActivity::class.java)
            intent.putExtra("nowDay", monthList[nowIdx].millis)
            requireActivity().startActivity(intent)
        }

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

        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
//    }

    override fun onResume() {
        super.onResume()
        setAdapter()
        getCategoryList()

        val date = SimpleDateFormat("yyyy,MM").format(millis)

        //모임 이벤트
        val eventService = ScheduleService()
        eventService.setGetMonthMoimScheduleView(this)
        eventService.getMonthMoimSchedule(date)

//        onBackPressedCallback.isEnabled = true
        Log.d(
            "CalendarMonth",
            "Schedule list : " + binding.calendarMonthView.getScheduleList().toString()
        )

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

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapter() {
        binding.homeDailyEventRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyEventRv.adapter = personalScheduleRVAdapter

        binding.homeDailyGroupEventRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyGroupEventRv.adapter = groupScheduleRVAdapter
//        if (nowIdx==0) setToday()

        personalScheduleRVAdapter.setContentClickListener(object :
            DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(schedule: Schedule) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", schedule)
                requireActivity().startActivity(intent)
            }
        })

        groupScheduleRVAdapter.setGorupContentClickListener(object :
            DailyGroupRVAdapter.GroupContentClickListener {
            override fun onGroupContentClick(event: Schedule) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("schedule", event)
                requireActivity().startActivity(intent)
            }
        })

        /** 기록 아이템 클릭 리스너 **/
        personalScheduleRVAdapter.setRecordClickListener(object :
            DailyPersonalRVAdapter.DiaryInterface {
            override fun onDetailClicked(schedule: Schedule) {

                val intent = Intent(context, PersonalDetailActivity::class.java)
                intent.putExtra("event", schedule)
                requireActivity().startActivity(intent)
            }
        })

        groupScheduleRVAdapter.setRecordClickListener(object : DailyGroupRVAdapter.DiaryInterface {
            override fun onGroupDetailClicked(monthDiary: MoimDiary?) {

                val intent = Intent(context, MoimMemoDetailActivity::class.java)
                intent.putExtra("groupDiary", monthDiary)
                requireActivity().startActivity(intent)
            }

        })
        /** ----- **/
    }

    private fun getCategoryList() {
        categoryList = setCategoryList(db)
        binding.calendarMonthView.setCategoryList(categoryList)
        personalScheduleRVAdapter.setCategory(categoryList)
        groupScheduleRVAdapter.setCategory(categoryList)
    }

    private fun setDaily(idx: Int) {
        binding.homeDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0, 0)
        setData(idx)
        Log.d("CHECK_GROUP_EVENT", monthGroupSchedule.toString())
    }

    private fun setData(idx: Int) {
        getGroupDiary()
        getSchedule(idx)
    }

    private fun setPersonalEmptyText() {
        if (schedulePersonal.size == 0) binding.homeDailyEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyEventNoneTv.visibility = View.GONE
    }

    private fun setMoimEmptyText() {
        if (scheduleMoim.size == 0) binding.homeDailyGroupEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyGroupEventNoneTv.visibility = View.GONE
    }

    private fun getSchedule(idx: Int) {
        schedulePersonal.clear()
        scheduleMoim.clear()
        val todayStart = (monthList[idx].withTimeAtStartOfDay().millis) / 1000
        val todayEnd = (monthList[idx].plusDays(1).withTimeAtStartOfDay().millis - 1) / 1000
        setPersonalSchedule(todayStart, todayEnd) // 개인 일정 표시
        setGroupSchedule(todayStart, todayEnd) // 그룹 일정 표시
    }

    private fun setPersonalSchedule(todayStart: Long, todayEnd: Long) {
        lifecycleScope.launch {
            viewModel.getDailySchedules(todayStart, todayEnd)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setGroupSchedule(todayStart: Long, todayEnd: Long) {
        scheduleMoim = monthGroupSchedule.filter { item -> item.startLong <= todayEnd && item.endLong >= todayStart } as ArrayList<Schedule>

        groupScheduleRVAdapter.addGroup(scheduleMoim)
        requireActivity().runOnUiThread {
//            Log.d("CalendarMonth", "Group Schedule : $scheduleMoim")
            groupScheduleRVAdapter.notifyDataSetChanged()
        }
        setMoimEmptyText()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getGroupDiary() {
        try {
            val date = SimpleDateFormat("yyyy,MM").format(millis)
            val service = DiaryService()
            service.getGroupMonthDiary2(date, 0, 50)
            service.getGroupMonthView(this@CalendarMonthFragment)
        } catch (e: java.lang.Exception) {
            Log.e("Exception", "Exception occurred: ${e.message}")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initObserve() {
        Log.d("getDailySchedules", "initObserve()")
        viewModel.scheduleList.observe(viewLifecycleOwner) {
            schedulePersonal.clear()
            if (!it.isNullOrEmpty()) {
                val dailySchedule = it as ArrayList<Schedule>
                schedulePersonal = dailySchedule.filter { item -> !item.moimSchedule } as ArrayList<Schedule>
            }
            personalScheduleRVAdapter.addPersonal(schedulePersonal)
            Log.d("getDailySchedules", "Personal Schedule : $schedulePersonal")
            requireActivity().runOnUiThread {
                personalScheduleRVAdapter.notifyDataSetChanged()
                setPersonalEmptyText()
            }
        }
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis: Long) = CalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
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

    override fun onGetMonthMoimScheduleSuccess(response: GetMonthScheduleResponse) {
        val result = response.result
        monthGroupSchedule = result.map { it.convertServerScheduleResponseToLocal() } as ArrayList
        Log.d("SUCCESS_MOIM", monthGroupSchedule.toString())

        val forDB: Thread = Thread {
            tempSchedule = db.scheduleDao.getScheduleMonth(
                monthList[0].withTimeAtStartOfDay().millis / 1000,
                monthList[41].plusDays(1).withTimeAtStartOfDay().millis / 1000
            ) as ArrayList<Schedule>
        }
        forDB.start()
        try {
            forDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        tempSchedule.addAll(monthGroupSchedule)
        Log.d("SUCCESS_MOIM_TEMP", tempSchedule.toString())


        binding.calendarMonthView.setScheduleList(tempSchedule)

    }

    override fun onGetMonthMoimScheduleFailure(message: String) {
        Log.d("GET_MONTH_EVENT", "Failure -> $message")
    }
}