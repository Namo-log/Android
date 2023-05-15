package com.example.namo.ui.bottom.group.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentGroupCalendarMonthBinding
import com.example.namo.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.ui.bottom.home.calendar.Calendar2View
import org.joda.time.DateTime

class GroupCalendarMonthFragment : Fragment() {
    lateinit var db : NamoDatabase
    private lateinit var binding : FragmentGroupCalendarMonthBinding

    private var millis : Long = 0L
    private var isShow = false
    private lateinit var monthList : List<DateTime>
    private lateinit var tempEvent : List<Event>

    private var prevIdx = -1
    private var nowIdx = 0
    private var event_personal : ArrayList<Event> = arrayListOf()
    private var event_group : ArrayList<Event> = arrayListOf()
    private val personalEventRVAdapter = DailyPersonalRVAdapter()
    private val groupEventRVAdapter = DailyGroupRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            millis = it.getLong(MILLIS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupCalendarMonthBinding.inflate(inflater, container, false)
        db = NamoDatabase.getInstance(requireContext())

        binding.groupCalendarMonthView.setDayList(millis)
        monthList = binding.groupCalendarMonthView.getDayList()

        binding.groupFab.setOnClickListener {
            Toast.makeText(requireContext(), "Click group Fab!", Toast.LENGTH_SHORT).show()
        }

        binding.groupCalendarMonthView.onDateClickListener = object : Calendar2View.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos: Int?) {
                if (date == null) Log.d("GROUP_CALENDAR", "The NULL clicked!")

                Log.d("GROUP_CALENDAR", "The $date is clicked!")
                binding.groupCalendarMonthView.selectedDate = date

                if (date != null && pos != null) {
                    nowIdx = pos
                    setDaily(nowIdx)

                    if (isShow && prevIdx == nowIdx) {
                        binding.constraintLayout2.transitionToStart()
                        isShow = !isShow
                    }
                    else if (!isShow) {
                        binding.constraintLayout2.transitionToEnd()
                        isShow = !isShow
                    }
                    prevIdx = nowIdx
                }

                binding.groupCalendarMonthView.invalidate()
            }
        }

        return binding.root
    }

    private fun setAdapter() {
        binding.groupDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyEventRv.adapter = personalEventRVAdapter

        binding.groupDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyGroupEventRv.adapter = groupEventRVAdapter

        personalEventRVAdapter.setContentClickListener(object : DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(event: Event) {
                Log.d("GROUP_CALENDAR", "개인 일정 클릭")
            }
        })
    }

    private fun setDaily(idx : Int) {
        binding.groupDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.groupScrollSv.scrollTo(0,0)
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

    override fun onResume() {
        super.onResume()
        setAdapter()
        var forDB : Thread = Thread {
            tempEvent = db.eventDao.getEventMonth(monthList[0].withTimeAtStartOfDay().millis, monthList[41].plusDays(1).withTimeAtStartOfDay().millis)
        }
        forDB.start()
        try {
            forDB.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
        binding.groupCalendarMonthView.setEventList(tempEvent)
    }

    override fun onPause() {
        super.onPause()
        val listener = object : Calendar2View.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos : Int?) {
                binding.groupCalendarMonthView.selectedDate = null
                binding.constraintLayout2.transitionToStart()
                isShow = false
                binding.constraintLayout2.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = GroupCalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}