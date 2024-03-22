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
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.data.remote.diary.DiaryService
import com.mongmong.namo.data.remote.diary.GetGroupMonthView
import com.mongmong.namo.data.remote.event.EventService
import com.mongmong.namo.domain.model.GetMonthEventResponse
import com.mongmong.namo.domain.model.GetMonthEventResult
import com.mongmong.namo.data.remote.event.GetMonthMoimEventView
import com.mongmong.namo.databinding.FragmentCalendarMonthBinding
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.MonthDiary
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.GroupDetailActivity
import com.mongmong.namo.presentation.ui.bottom.diary.mainDiary.PersonalDetailActivity
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
class CalendarMonthFragment : Fragment(), GetGroupMonthView, GetMonthMoimEventView {

    lateinit var db: NamoDatabase
    private lateinit var binding: FragmentCalendarMonthBinding
    private lateinit var categoryList: List<Category>

    private var millis: Long = 0L
    var isShow = false
    private lateinit var monthList: List<DateTime>
    private lateinit var tempEvent: ArrayList<Event>
    private var monthGroupEvent: ArrayList<Event> = arrayListOf()

    private var prevIdx = -1
    private var nowIdx = 0
    private var event_personal: ArrayList<Event> = arrayListOf()
    private var event_group: ArrayList<Event> = arrayListOf()
    private val personalEventRVAdapter = DailyPersonalRVAdapter()
    private val groupEventRVAdapter = DailyGroupRVAdapter()

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
        val eventService = EventService()
        eventService.setGetMonthMoimEventView(this)
        eventService.getMonthMoimEvent(date)

//        onBackPressedCallback.isEnabled = true
        Log.d(
            "CalendarMonth",
            "Event list : " + binding.calendarMonthView.getEventList().toString()
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
        binding.homeDailyEventRv.adapter = personalEventRVAdapter

        binding.homeDailyGroupEventRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyGroupEventRv.adapter = groupEventRVAdapter
//        if (nowIdx==0) setToday()

        personalEventRVAdapter.setContentClickListener(object :
            DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(event: Event) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("event", event)
                requireActivity().startActivity(intent)
            }
        })

        groupEventRVAdapter.setGorupContentClickListener(object :
            DailyGroupRVAdapter.GroupContentClickListener {
            override fun onGroupContentClick(event: Event) {
                val intent = Intent(context, ScheduleActivity::class.java)
                intent.putExtra("event", event)
                requireActivity().startActivity(intent)
            }
        })

        /** 기록 아이템 클릭 리스너 **/
        personalEventRVAdapter.setRecordClickListener(object :
            DailyPersonalRVAdapter.DiaryInterface {
            override fun onDetailClicked(event: Event) {

                val intent = Intent(context, PersonalDetailActivity::class.java)
                intent.putExtra("event", event)
                requireActivity().startActivity(intent)
            }
        })

        groupEventRVAdapter.setRecordClickListener(object : DailyGroupRVAdapter.DiaryInterface {
            override fun onGroupDetailClicked(monthDiary: MonthDiary?) {

                val intent = Intent(context, GroupDetailActivity::class.java)
                intent.putExtra("groupDiary", monthDiary)
                requireActivity().startActivity(intent)
            }

        })
        /** ----- **/
    }

    private fun getCategoryList() {
        categoryList = setCategoryList(db)
        binding.calendarMonthView.setCategoryList(categoryList)
        personalEventRVAdapter.setCategory(categoryList)
        groupEventRVAdapter.setCategory(categoryList)
    }

    private fun setDaily(idx: Int) {
        binding.homeDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0, 0)
        setData(idx)
        Log.d("CHECK_GROUP_EVENT", monthGroupEvent.toString())
    }

    private fun setData(idx: Int) {
        getGroupDiary()
        getEvent(idx)
    }

    private fun setPersonalEmptyMsg() {
        if (event_personal.size == 0) binding.homeDailyEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyEventNoneTv.visibility = View.GONE
    }

    private fun setGroupEmptyMsg() {
        if (event_group.size == 0) binding.homeDailyGroupEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyGroupEventNoneTv.visibility = View.GONE
    }

    private fun getEvent(idx: Int) {
        event_personal.clear()
        event_group.clear()
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
        event_group = monthGroupEvent.filter { item -> item.startLong <= todayEnd && item.endLong >= todayStart } as ArrayList<Event>

        groupEventRVAdapter.addGroup(event_group)
        requireActivity().runOnUiThread {
            Log.d("CalendarMonth", "Group Event : $event_group")
            groupEventRVAdapter.notifyDataSetChanged()
        }
        setGroupEmptyMsg()
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
            if (!it.isNullOrEmpty()) {
                val dailyEvent = it as ArrayList<Event>
                event_personal = dailyEvent.filter { item -> !item.moimSchedule } as ArrayList<Event>

                personalEventRVAdapter.addPersonal(event_personal)
                Log.d("CalendarMonth", "getDailySchedules Personal Event : $event_personal")
                requireActivity().runOnUiThread {
                    personalEventRVAdapter.notifyDataSetChanged()
                    setPersonalEmptyMsg()
                }
            }
        }
    }

    companion object {
        private const val MILLIS = "MILLIS"

        const val IS_UPLOAD = true
        const val IS_NOT_UPLOAD = false

        fun newInstance(millis: Long) = CalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }

    private fun serverToEvent(schedule: GetMonthEventResult): Event {
        return Event(
            0,
            schedule.name,
            schedule.startDate,
            schedule.endDate,
            schedule.interval,
            schedule.categoryId,
            schedule.locationName,
            schedule.x,
            schedule.y,
            0,
            schedule.alarmDate ?: listOf(),
            IS_UPLOAD,
            RoomState.DEFAULT.state,
            schedule.scheduleId,
            schedule.categoryId,
            if (schedule.hasDiary) 1 else 0,
            schedule.moimSchedule
        )
    }

    override fun onGetGroupMonthSuccess(response: DiaryGetMonthResponse) {
        val data = response.result
        groupEventRVAdapter.addGroupDiary(data.content as ArrayList<MonthDiary>)
    }

    override fun onGetGroupMonthFailure(message: String) {
        Log.d("GET_GROUP_MONTH", message)
    }

    override fun onGetMonthMoimEventSuccess(response: GetMonthEventResponse) {
        val result = response.result
        monthGroupEvent = result.map { serverToEvent(it) } as ArrayList
        Log.d("SUCCESS_MOIM", monthGroupEvent.toString())

        var forDB: Thread = Thread {
            tempEvent = db.eventDao.getEventMonth(
                monthList[0].withTimeAtStartOfDay().millis / 1000,
                monthList[41].plusDays(1).withTimeAtStartOfDay().millis / 1000
            ) as ArrayList<Event>
        }
        forDB.start()
        try {
            forDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        tempEvent.addAll(monthGroupEvent)
        Log.d("SUCCESS_MOIM_TEMP", tempEvent.toString())


        binding.calendarMonthView.setEventList(tempEvent)

    }

    override fun onGetMonthMoimEventFailure(message: String) {
        Log.d("GET_MONTH_EVENT", "Failure -> $message")
    }
}