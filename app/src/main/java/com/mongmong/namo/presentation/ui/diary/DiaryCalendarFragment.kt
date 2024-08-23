package com.mongmong.namo.presentation.ui.diary

import CalendarDay
import DiaryCalendarAdapter
import android.util.Log
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class DiaryCalendarFragment :
    BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar),
    DiaryCalendarAdapter.OnCalendarDayClickListener {


    private val viewModel: DiaryCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private var currentDay: CalendarDay? = null

    override fun setup() {
        setCalendar()
        initObserve()
    }

    private fun setCalendar() {
        val calendarItems = generateCalendarItems()

        binding.diaryCalendarRv.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = DiaryCalendarAdapter(calendarItems, this)
        binding.diaryCalendarRv.adapter = calendarAdapter

        scrollToToday()
    }

    private fun initObserve() {
        viewModel.isBottomSheetOpened.observe(viewLifecycleOwner) { isOpened ->
            if(isOpened) binding.diaryCalendarMl.transitionToStart()
            else binding.diaryCalendarMl.transitionToEnd()
            calendarAdapter.updateAllItems(isOpened)
        }
    }

    private fun generateCalendarItems(): List<CalendarDay> {
        val calendarItems = mutableListOf<CalendarDay>()

        val calendar = Calendar.getInstance()
        calendar.set(1900, Calendar.JANUARY, 1)

        val endDate = Calendar.getInstance().apply {
            set(2100, Calendar.DECEMBER, 31)
        }

        while (calendar.before(endDate) || calendar == endDate) {
            val isFirstDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) == 1
            val dateText = if (isFirstDayOfMonth) {
                "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
            } else {
                "${calendar.get(Calendar.DAY_OF_MONTH)}"
            }
            calendarItems.add(CalendarDay(dateText, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return calendarItems
    }

    private fun scrollToToday() {
        val today = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply {
            set(1900, Calendar.JANUARY, 1)
        }

        val daysFromStart = ((today.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        binding.diaryCalendarRv.scrollToPosition(daysFromStart)
    }

    override fun onCalendarDayClick(calendarDay: CalendarDay) {
        viewModel.toggleBottomSheetState()
        currentDay = calendarDay
        binding.bottomSheetTv.text = "${calendarDay.year}/${calendarDay.month + 1}/${calendarDay.date}"
    }

}
