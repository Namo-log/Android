package com.example.namo.ui.bottom.home.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.namo.R
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentCalendarMonthBinding
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

//        binding.calendarMonthView.initCalendar(DateTime(millis), getMonthList(DateTime(millis)))

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.d("CALENDAR_MONTH_FRAG","onResume")
        binding.calendarMonthView.initCalendar(DateTime(millis), getMonthList(DateTime(millis)))
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