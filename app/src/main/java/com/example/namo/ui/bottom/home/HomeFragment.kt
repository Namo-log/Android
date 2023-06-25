package com.example.namo.ui.bottom.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.databinding.FragmentHomeBinding
import com.example.namo.ui.bottom.home.calendar.SetMonthDialog
import com.example.namo.ui.bottom.home.calendar.CalendarAdapter
import org.joda.time.DateTime


class HomeFragment : Fragment() {

    private lateinit var calendarAdapter : CalendarAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var monthList : List<DateTime>

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2
    private var prevIdx = -1

    lateinit var db : NamoDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container,false)
        db = NamoDatabase.getInstance(requireContext())
        calendarAdapter = CalendarAdapter(context as MainActivity)

        binding.homeCalendarTodayTv.text = DateTime().dayOfMonth.toString()

        binding.homeCalendarVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.homeCalendarVp.adapter = calendarAdapter
        binding.homeCalendarVp.setCurrentItem(CalendarAdapter.START_POSITION, false)
        binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")

        binding.homeCalendarVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                Log.d("VIEWPAGER_PAGE_SELECTED", "position : $position")
                pos = position
                prevIdx = -1
                millis = binding.homeCalendarVp.adapter!!.getItemId(position)
                binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
                super.onPageSelected(position)
            }
        })

        clickListener()
        Log.e("HOME_LIFECYCLE", "OnCreateView")

        return binding.root
    }


    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    private fun clickListener() {

        binding.homeCalendarYearMonthTv.setOnClickListener {
            SetMonthDialog(requireContext(), millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.homeCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }

        binding.homeCalendarTodayTv.setOnClickListener {
            binding.homeCalendarVp.setCurrentItem(todayPos, true)
        }
    }
}