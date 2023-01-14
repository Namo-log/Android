package com.example.namo.bottom.home.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.namo.R
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import com.example.namo.bottom.home.calendar.events.Event
import com.example.namo.databinding.FragmentCalendarMonthBinding
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import org.joda.time.DateTime

class CalendarMonthFragment : Fragment() {

    private var millis : Long = 0L

    private lateinit var binding : FragmentCalendarMonthBinding
    private var eventList = ArrayList<Event>()

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
        binding = DataBindingUtil.inflate<FragmentCalendarMonthBinding>(inflater, R.layout.fragment_calendar_month, container, false)

        getEventList()
        binding.calendarMonthView.initCalendar(DateTime(millis), getMonthList(DateTime(millis)), eventList)



        return binding.root
    }

    private fun getEventList() {
        eventList.apply {
            add(
                Event(
                    "오늘내일",
                    startLong = System.currentTimeMillis(),
                    endLong = DateTime(System.currentTimeMillis()).plusDays(1).millis,
                    getInterval(System.currentTimeMillis(), DateTime(System.currentTimeMillis()).plusDays(1).millis),
                    R.color.palette3
                )
            )
            add(
                Event(
                    "내일 모레",
                    DateTime(System.currentTimeMillis()).plusDays(1).millis,
                    DateTime(System.currentTimeMillis()).plusDays(2).millis,
                    getInterval(DateTime(System.currentTimeMillis()).plusDays(1).millis, DateTime(System.currentTimeMillis()).plusDays(2).millis),
                    R.color.palette1
                )
            )
            add(
                Event(
                    "어제 모레",
                    DateTime(System.currentTimeMillis()).minusDays(1).millis,
                    DateTime(System.currentTimeMillis()).plusDays(2).millis,
                    getInterval(DateTime(System.currentTimeMillis()).minusDays(1).millis, DateTime(System.currentTimeMillis()).plusDays(2).millis),
                    R.color.palette4
                )
            )
            add(
                Event(
                    "오늘오늘",
                    startLong = System.currentTimeMillis(),
                    endLong = System.currentTimeMillis(),
                    getInterval(System.currentTimeMillis(), System.currentTimeMillis()),
                    R.color.palette6
                )
            )
            add(
                Event(
                    "가나다라마바사",
                    startLong = System.currentTimeMillis(),
                    endLong = System.currentTimeMillis(),
                    getInterval(System.currentTimeMillis(), System.currentTimeMillis()),
                    R.color.palette6
                )
            )
            add(
                Event(
                    "그제 어제 그제 어제 그제",
                    DateTime(System.currentTimeMillis()).minusDays(2).millis,
                    DateTime(System.currentTimeMillis()).minusDays(1).millis,
                    getInterval(DateTime(System.currentTimeMillis()).minusDays(2).millis, DateTime(System.currentTimeMillis()).minusDays(1).millis),
                    R.color.palette10
                )
            )
        }
        Log.d("BEFORE_SORT_EVENT", eventList.toString())

        sortEventList()
    }

    private fun sortEventList() {
        eventList.sortByDescending(Event::interval)
        Log.d("SORT_EVENT", eventList.toString())
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = CalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}