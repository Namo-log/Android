package com.mongmong.namo.presentation.ui.home

import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentHomeBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.diary.DiaryActivity
import com.mongmong.namo.presentation.utils.SetMonthDialog
import com.mongmong.namo.presentation.ui.home.calendar.CalendarAdapter
import org.joda.time.DateTime

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2
    private var prevIdx = -1

    override fun setup() {
        setView()
        initClickListeners()
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

    private fun initClickListeners() {
        // 상단 날짜 클릭
        binding.homeCalenderYearMonthLayout.setOnClickListener {
            SetMonthDialog(requireContext(), millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.homeCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }

        // 오늘 날짜 클릭
        binding.homeCalendarTodayTv.setOnClickListener {
            // 오늘 날짜가 있는 달력으로 이동
            binding.homeCalendarVp.setCurrentItem(todayPos, true)
        }

        // 보관함 아이콘 클릭
        binding.homeCalendarDiaryCollectBtn.setOnClickListener {
            startActivity(Intent(requireContext(), DiaryActivity::class.java))
        }
    }

    companion object {
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}