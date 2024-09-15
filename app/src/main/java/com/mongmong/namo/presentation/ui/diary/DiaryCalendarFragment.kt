package com.mongmong.namo.presentation.ui.diary

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryCalendarAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx

@AndroidEntryPoint
class DiaryCalendarFragment :
    BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar),
    DiaryCalendarAdapter.OnCalendarDayClickListener {

    private val viewModel: DiaryCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private var isInitialLoad = true
    private var lastDisplayedMonth: Int? = null // 마지막으로 표시된 월을 저장하는 변수
    private val fetchedMonths = mutableSetOf<String>() // 이미 요청한 월을 저장하는 집합

    override fun setup() {
        binding.viewModel = viewModel
        setCalendar()
        initObserve()
    }

    private fun setCalendar() {
        val calendarItems = generateCalendarItems()
        calendarAdapter =
            DiaryCalendarAdapter(binding.diaryCalendarRv, calendarItems, this@DiaryCalendarFragment)

        binding.diaryCalendarRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
            addItemDecoration(
                DiaryCalendarItemDecoration(
                    dpToPx(requireContext(), 16f),
                    dpToPx(requireContext(), 16f)
                )
            )
        }

        setupScrollListener()
        scrollToToday()
        setDiaryIndicator(binding.diaryCalendarRv)
    }

    private fun setupScrollListener() {
        binding.diaryCalendarRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                checkFirstDay(recyclerView)
                setDiaryIndicator(recyclerView)
            }
        })
    }

    private fun checkFirstDay(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
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
                        showMonthSnackBar(calendarDay.year, currentMonth)
                    }
                    break
                }
            }
        }
    }

    private fun setDiaryIndicator(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        //val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val lastVisiblePosition = firstVisiblePosition + INDICATOR_FIRST

        getCalendarDiary(firstVisiblePosition)
        getCalendarDiary(lastVisiblePosition)
    }

    private fun getCalendarDiary(position: Int) {
        val calendarDay = calendarAdapter.getItemAtPosition(position)
        if (calendarDay != null) {
            val yearMonth = "${calendarDay.year}-${String.format("%02d", calendarDay.month + 1)}" // yyyy-MM 형식

            // 이미 요청한 적 없는 월인 경우 서버에 요청
            if (!fetchedMonths.contains(yearMonth)) {
                fetchedMonths.add(yearMonth) // 이미 요청한 월은 다시 요청하지 않음
                viewModel.getCalendarDiary(yearMonth)
            }
        }
    }

    @SuppressLint("RestrictedApi", "ResourceAsColor")
    private fun showMonthSnackBar(year: Int, month: Int) {
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
        viewModel.calendarDiaryResult.observe(viewLifecycleOwner) { calendarDiaryResult ->
            // 어댑터에 기록이 있는 날짜 정보 전달
            val yearMonth = "${calendarDiaryResult.year}-${String.format("%02d", calendarDiaryResult.month)}"

            // 이미 리스트 타입이므로 split 대신 그대로 사용
            val recordDates = calendarDiaryResult.dates.toSet()  // 만약 List<Int>라면 바로 Set으로 변환

            calendarAdapter.updateDiaryDates(yearMonth, recordDates)
        }



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

        val calendar = Calendar.getInstance().apply {
            set(1970, Calendar.JANUARY, 1)
        }

        val endCalendar = Calendar.getInstance().apply {
            //set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(1971, Calendar.JANUARY, 1)
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1))

        // 시작 날짜부터 현재 날짜까지 calendarItems에 추가
        while (calendar.before(endCalendar) || calendar == endCalendar) {
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
            set(1970, Calendar.JANUARY, 1)
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
        const val INDICATOR_CALENDAR_MIDDLE = 21
    }
}
