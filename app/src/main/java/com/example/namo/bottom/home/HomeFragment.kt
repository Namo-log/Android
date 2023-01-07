package com.example.namo.bottom.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.bottom.home.calendar.CalendarAdapter
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.bottom.home.calendar.SetMonthDialog
import com.example.namo.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime


class HomeFragment : Fragment() {

    private lateinit var calendarAdapter : CalendarAdapter
    private lateinit var binding: FragmentHomeBinding

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container,false)
        calendarAdapter = CalendarAdapter(context as MainActivity)

        //hideBottomNavigation(false)

        binding.homeCalendarTodayTv.text = DateTime().dayOfMonth.toString()

        binding.homeCalendarVp.adapter = calendarAdapter
        binding.homeCalendarVp.setCurrentItem(CalendarAdapter.START_POSITION, false)
        setMillisText()

        binding.homeCalendarVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                Log.d("VIEWPAGER_PAGE_SELECTED", "position : $position")
                pos = position
                millis = binding.homeCalendarVp.adapter!!.getItemId(position)
                setMillisText()
                super.onPageSelected(position)
            }
        })

        clickListener()

        return binding.root
    }

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

    private fun setMillisText() {
        binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
    }

//    private fun hideBottomNavigation( bool : Boolean){
//        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
//        if(bool == true) {
//            bottomNavigationView.visibility = View.GONE
//        } else {
//            bottomNavigationView.visibility = View.VISIBLE
//        }
//    }
}