package com.example.namo.ui.bottom.group.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentGroupCalendarBinding
import com.example.namo.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.utils.CalendarUtils
import org.joda.time.DateTime

class GroupCalendarFragment : Fragment() {

    private lateinit var calendarAdapter : GroupCalendarAdapter
    private lateinit var binding: FragmentGroupCalendarBinding
    private lateinit var monthList : List<DateTime>

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1
    private var nowIdx = 0

    private var event_personal : ArrayList<Event> = arrayListOf()
    private var event_group : ArrayList<Event> = arrayListOf()

    private val personalEventRVAdapter = DailyPersonalRVAdapter()
    private val groupEventRVAdapter = DailyGroupRVAdapter()

    lateinit var db : NamoDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_calendar, container, false)
        db = NamoDatabase.getInstance(requireContext())
        calendarAdapter = GroupCalendarAdapter(context as MainActivity)

        binding.groupCalendarTodayTv.text = DateTime().dayOfMonth.toString()

        binding.groupCalendarVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.groupCalendarVp.adapter = calendarAdapter
        binding.groupCalendarVp.setCurrentItem(GroupCalendarAdapter.START_POSITION, false)
        setMillisText()

        binding.groupCalendarVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pos = position
                prevIdx = -1
                millis = binding.groupCalendarVp.adapter!!.getItemId(position)
                setMillisText()
                super.onPageSelected(position)
            }
        })

        clickListener()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    private fun clickListener() {
        binding.groupCalendarYearMonthTv.setOnClickListener {
            Log.d("GROUP_CALENDAR_FRAGMENT","year month click")
        }

        binding.groupCalendarTodayTv.setOnClickListener {
            binding.groupCalendarVp.setCurrentItem(todayPos, true)
            setToday()
        }
    }

    private fun setAdapter() {
        binding.groupDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyEventRv.adapter = personalEventRVAdapter

        binding.groupDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyGroupEventRv.adapter = groupEventRVAdapter
        if (nowIdx==0) setToday()

        personalEventRVAdapter.setContentClickListener(object : DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(event: Event) {
                Log.d("GROUP_CALENDAR_FRAGMENT","개인 일정 클릭")
            }
        })
    }

    private fun setToday() {
        monthList = CalendarUtils.getMonthList(DateTime(System.currentTimeMillis()))
        prevIdx = monthList.indexOf(DateTime(System.currentTimeMillis()).withTimeAtStartOfDay())
        nowIdx = prevIdx
        setDaily(nowIdx)
    }

    private fun setDaily(idx : Int) {
        binding.groupDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.groupDailyScrollSv.scrollTo(0,0)
        setData(idx)
    }

    private fun setData(idx : Int) {
        getEvent(idx)

        personalEventRVAdapter.addPersonal(event_personal)
        groupEventRVAdapter.addGroup(event_group)
        setEmptyMsg()
    }

    private fun setEmptyMsg() {
        if (event_personal.size == 0 ) binding.groupDailyEventNoneTv.visibility = View.VISIBLE
        else binding.groupDailyEventNoneTv.visibility = View.GONE

        if (event_group.size == 0) binding.groupDailyGroupEventNoneTv.visibility = View.VISIBLE
        else binding.groupDailyGroupEventNoneTv.visibility = View.GONE
    }

    private fun getEvent(idx : Int) {
        event_personal.clear()
        event_group.clear()
        var todayStart = monthList[idx].withTimeAtStartOfDay().millis
        var todayEnd = monthList[idx].plusDays(1).withTimeAtStartOfDay().millis - 1

        var forPersonalEvent : Thread = Thread {
            event_personal = db.eventDao.getEventDaily(todayStart, todayEnd) as ArrayList<Event>
            personalEventRVAdapter.addPersonal(event_personal)
            requireActivity().runOnUiThread {
                Log.d("NOTIFY", event_personal.toString())
                personalEventRVAdapter.notifyDataSetChanged()
            }
        }
        forPersonalEvent.start()

        try {
            forPersonalEvent.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setMillisText() {
        binding.groupCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
        monthList = CalendarUtils.getMonthList(DateTime(millis))
        prevIdx = CalendarUtils.getPrevOffset(DateTime(millis).withDayOfMonth(1))
        nowIdx = prevIdx
        setDaily(nowIdx)
    }
}