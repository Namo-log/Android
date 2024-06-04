package com.mongmong.namo.presentation.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.databinding.FragmentHomeBinding
import com.mongmong.namo.presentation.utils.SetMonthDialog
import com.mongmong.namo.presentation.ui.home.calendar.CalendarAdapter
import org.joda.time.DateTime

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2
    private var prevIdx = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container,false)

        setView()
        clickListener()

        return binding.root
    }

    private fun setView() {
        with(binding) {
            homeCalendarTodayTv.text = DateTime().dayOfMonth.toString()
            homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")

            homeCalendarVp.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = CalendarAdapter(requireActivity())
                setCurrentItem(CalendarAdapter.START_POSITION, false)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        Log.d("VIEWPAGER_PAGE_SELECTED", "position : $position")
                        pos = position
                        prevIdx = -1
                        millis = binding.homeCalendarVp.adapter!!.getItemId(position)
                        binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
                        super.onPageSelected(position)
                    }
                })
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    private fun clickListener() {
        binding.homeCalenderYearMonthLayout.setOnClickListener {
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

    companion object {
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}