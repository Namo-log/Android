package com.example.namo.bottom.home.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.namo.R
import com.example.namo.databinding.FragmentCalendarMonthBinding
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import org.joda.time.DateTime

class CalendarMonthFragment : Fragment() {

    private var millis : Long = 0L

    private lateinit var binding : FragmentCalendarMonthBinding

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

        binding.millis.text = DateTime(millis).toString("yyyy.MM")
        binding.calendarMonthView.initCalendar(DateTime(millis), getMonthList(DateTime(millis)))

        clickListener()

        return binding.root
    }

    private fun clickListener() {
        binding.millis.setOnClickListener {
            SetMonthDialog(requireContext(), millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)

                Log.d("TEST", result.toString())
            }.show()
        }
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