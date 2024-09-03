package com.mongmong.namo.presentation.ui.diary

import android.annotation.SuppressLint
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.diary.adapter.CalendarDay
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryCalendarAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx

@AndroidEntryPoint
class DiaryCalendarFragment :
    BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar),
    DiaryCalendarAdapter.OnCalendarDayClickListener {

    private val viewModel: DiaryCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private var isInitialLoad = true
    private var lastDisplayedMonth: Int? = null // 마지막으로 표시된 월을 저장하는 변수

    override fun setup() {
        binding.viewModel = viewModel
        setCalendar()
        initObserve()
        setupScrollListener()
    }

    private fun setCalendar() {
        val calendarItems = generateCalendarItems()

        binding.diaryCalendarRv.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarAdapter = DiaryCalendarAdapter(binding.diaryCalendarRv, calendarItems, this)
        binding.diaryCalendarRv.adapter = calendarAdapter

        scrollToToday()
    }

    private fun setupScrollListener() {
        binding.diaryCalendarRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition() + INDICATOR_FIRST
                val lastVisiblePosition = firstVisiblePosition + INDICATOR_LAST

                // 한 주(7일)의 아이템을 검사
                for (position in firstVisiblePosition..lastVisiblePosition) {
                    val calendarDay = calendarAdapter.getItemAtPosition(position)
                    if (calendarDay != null) {
                        // 날짜가 1일인 아이템을 찾음
                        if (calendarDay.date.endsWith("/1")) {
                            val currentMonth = calendarDay.month + 1

                            // 달이 바뀐 경우에만 스낵바를 띄움
                            if (lastDisplayedMonth == null || lastDisplayedMonth != currentMonth) {
                                lastDisplayedMonth = currentMonth
                                handleMonthChange(calendarDay.year, currentMonth)
                            }
                            break
                        }
                    }
                }
            }
        })
    }


    @SuppressLint("RestrictedApi", "ResourceAsColor")
    private fun handleMonthChange(year: Int, month: Int) {
        val snackbar = Snackbar.make(binding.root, "${year}년 ${month}월", Snackbar.LENGTH_SHORT)

        // TextView의 레이아웃 파라미터 및 마진 조정
        (snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView).apply {
            background = getDrawable(requireContext(), R.drawable.bg_snackbar)
            setTextAppearance(R.style.calendar_snackbar)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(dpToPx(requireContext(), 24f).toInt(), dpToPx(requireContext(), 12f).toInt(), dpToPx(requireContext(), 24f).toInt(), dpToPx(requireContext(), 12f).toInt())
            layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                width = LinearLayout.LayoutParams.WRAP_CONTENT
                height = LinearLayout.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER
            }
        }

        // Snackbar의 위치 설정
        (snackbar.view as Snackbar.SnackbarLayout).setBackgroundColor(android.graphics.Color.TRANSPARENT)
        snackbar.view.layoutParams = (snackbar.view.layoutParams as FrameLayout.LayoutParams).apply {
            width = FrameLayout.LayoutParams.WRAP_CONTENT
            height = FrameLayout.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dpToPx(requireContext(), 148f).toInt()
        }

        snackbar.show()
    }





    private fun initObserve() {
        viewModel.isBottomSheetOpened.observe(viewLifecycleOwner) { isOpening ->
            if (isInitialLoad) {
                isInitialLoad = false
                return@observe // 초기 로드일 경우 무시
            }
            calendarAdapter.updateBottomSheetState(isOpening)
            if (isOpening) {
                binding.diaryCalendarMl.transitionToEnd()
            } else {
                binding.diaryCalendarMl.transitionToStart()
            }
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

    override fun onCalendarDayClick(date: CalendarDay) {
        viewModel.toggleBottomSheetState()
        viewModel.setSelectedDate(date)
    }

    companion object {
        const val INDICATOR_FIRST = 7
        const val INDICATOR_LAST = 13
    }
}
