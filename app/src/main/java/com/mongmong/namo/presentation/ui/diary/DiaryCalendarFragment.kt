package com.mongmong.namo.presentation.ui.diary

import CalendarDay
import DiaryCalendarAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class DiaryCalendarFragment : BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar) {

    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private var currentMonth: Int? = null
    private var currentToast: Toast? = null // 기존 토스트를 저장하는 변수

    override fun setup() {
        setCalendar()
    }

    private fun setCalendar() {
        val calendarItems = generateCalendarItems()

        binding.diaryCalendarRv.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = DiaryCalendarAdapter(calendarItems)
        binding.diaryCalendarRv.adapter = calendarAdapter

        binding.diaryCalendarRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll()
            }
        })

        scrollToToday()
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

    private fun handleScroll() {
        val layoutManager = binding.diaryCalendarRv.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()

        if (firstVisiblePosition != RecyclerView.NO_POSITION) {
            val calendarDay = calendarAdapter.getItem(firstVisiblePosition)
            val month = calendarDay.month
            val year = calendarDay.year

            if (month != currentMonth) {
                currentMonth = month
                showDateToast(year, month + 1) // 여기서 +1로 수정
            }
        }
    }

    private fun showDateToast(year: Int, month: Int) {
        // 기존 토스트가 있으면 취소
        currentToast?.cancel()

        // 새로운 토스트 생성 및 표시
        currentToast = Toast.makeText(requireContext(), "${year}년 ${month}월", Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    private fun scrollToToday() {
        val today = Calendar.getInstance()
        val startCalendar = Calendar.getInstance().apply {
            set(1900, Calendar.JANUARY, 1)
        }

        val daysFromStart = ((today.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        binding.diaryCalendarRv.scrollToPosition(daysFromStart)
    }
}
