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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.diary.adapter.DiaryCalendarAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.presentation.ui.diary.adapter.MoimDiaryRVAdapter
import com.mongmong.namo.presentation.ui.diary.adapter.PersonalDiaryRVAdapter
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx

@AndroidEntryPoint
class DiaryCalendarFragment :
    BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar),
    DiaryCalendarAdapter.OnCalendarDayClickListener {

    private val viewModel: DiaryCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private lateinit var personalDiaryAdapter: PersonalDiaryRVAdapter
    private lateinit var moimDiaryAdapter: MoimDiaryRVAdapter
    private var isInitialLoad = true
    private var lastDisplayedMonth: Int? = null // 마지막으로 표시된 월을 저장하는 변수
    private val fetchedMonths = mutableSetOf<String>() // 이미 요청한 월을 저장하는 집합

    override fun setup() {
        binding.viewModel = viewModel
        setCalendar()
        setupDiaryAdapters()
        initClickListener()
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

    private fun setupDiaryAdapters() {
        personalDiaryAdapter = PersonalDiaryRVAdapter()
        moimDiaryAdapter = MoimDiaryRVAdapter()

        binding.bottomSheetPersonalDiaryRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = personalDiaryAdapter
        }

        binding.bottomSheetMoimDiaryRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moimDiaryAdapter
        }
    }

    private fun initClickListener() {
        binding.diaryCalendarReturnBtn.setOnClickListener {
            binding.diaryCalendarRv.smoothScrollToPosition(calendarAdapter.itemCount - 1)
        }
    }

    private fun setupScrollListener() {
        binding.diaryCalendarRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                checkFirstDay(recyclerView)
                setDiaryIndicator(recyclerView)
                updateReturnBtnVisible()
            }
        })
    }

    private fun updateReturnBtnVisible() {
        val layoutManager = binding.diaryCalendarRv.layoutManager as GridLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        val isAtBottom = lastVisibleItemPosition == totalItemCount - 1
        val isBottomSheetOpened = viewModel.isBottomSheetOpened.value ?: false

        viewModel.setReturnBtnVisible(!isAtBottom && !isBottomSheetOpened)
    }

    private fun checkFirstDay(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = firstVisiblePosition + INDICATOR_LAST

        for (position in firstVisiblePosition..lastVisiblePosition) {
            val calendarDay = calendarAdapter.getItemAtPosition(position)
            if (calendarDay != null) {
                if (calendarDay.date == 1) {
                    val currentMonth = calendarDay.month + 1

                    // 달이 바뀐 경우에만 스낵바
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
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

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
            val yearMonth = "${calendarDiaryResult.year}-${String.format("%02d", calendarDiaryResult.month)}"
            val diaryDates = calendarDiaryResult.dates.toSet()

            calendarAdapter.updateDiaryDates(yearMonth, diaryDates)
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
            updateReturnBtnVisible()
        }

        viewModel.diariesByDate.observe(viewLifecycleOwner) { diaryItems ->
            val personalDiaries = diaryItems.filter { it.scheduleType == 0 }
            val moimDiaries = diaryItems.filter { it.scheduleType == 1 }

            // 필터링된 결과로 각 RecyclerView 업데이트
            personalDiaryAdapter.updateData(personalDiaries)
            moimDiaryAdapter.updateData(moimDiaries)

            binding.bottomSheetPersonalDiaryNoneTv.visibility = if (personalDiaries.isEmpty()) View.VISIBLE else View.GONE
            binding.bottomSheetMoimDiaryNoneTv.visibility = if (moimDiaries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun generateCalendarItems(): List<CalendarDay> {
        val calendarItems = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance().apply {
            set(1970, Calendar.JANUARY, 1)
        }
        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            //set(1971, Calendar.JANUARY, 1)
        }

        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToFill = firstDayOfWeek - 1

        for (i in 1..daysToFill) {
            calendarItems.add(CalendarDay(0, 0, 0, isEmpty = true))
        }

        while (calendar.before(endCalendar) || calendar == endCalendar) {
            calendarItems.add(CalendarDay(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)))
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
        if (viewModel.isBottomSheetOpened.value == true) {
            if (viewModel.selectedDate != null && viewModel.selectedDate.isSameDate(date)) {
                viewModel.toggleBottomSheetState()
            } else {
                viewModel.setSelectedDate(date)
                viewModel.getDiaryByDate(date)
            }
        } else {
            viewModel.setSelectedDate(date)
            viewModel.getDiaryByDate(date)
            viewModel.toggleBottomSheetState()
        }
    }

    companion object {
        const val INDICATOR_FIRST = 7
        const val INDICATOR_LAST = 21
    }
}
