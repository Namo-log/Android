package com.gradu.presentation.ui.home.diary

import android.annotation.SuppressLint
import android.content.Intent
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentDiaryCalendarBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.gradu.presentation.ui.home.diary.adapter.DiaryCalendarAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import com.google.android.material.snackbar.Snackbar
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.presentation.config.Constants.START_YEAR
import com.gradu.presentation.ui.community.moim.diary.MoimDiaryDetailActivity
import com.gradu.presentation.ui.home.diary.adapter.MoimDiaryRVAdapter
import com.gradu.presentation.ui.home.diary.adapter.PersonalDiaryRVAdapter
import com.gradu.presentation.utils.CalendarUtils.Companion.dpToPx
import com.mongmong.namo.presentation.utils.converter.DiaryDateConverter.toYearMonth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiaryCalendarFragment :
    BaseFragment<FragmentDiaryCalendarBinding>(R.layout.fragment_diary_calendar),
    DiaryCalendarAdapter.OnCalendarListener {

    private val viewModel: DiaryCalendarViewModel by viewModels()
    private lateinit var calendarAdapter: DiaryCalendarAdapter
    private lateinit var personalDiaryAdapter: PersonalDiaryRVAdapter
    private lateinit var moimDiaryAdapter: MoimDiaryRVAdapter
    private var isInitialLoad = true
    private var lastDisplayedMonth: String? = null // 마지막으로 표시된 월을 저장
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
        personalDiaryAdapter = PersonalDiaryRVAdapter(onItemClick = { scheduleId ->
            startActivity(Intent(requireContext(), PersonalDiaryDetailActivity::class.java)
                .putExtra("scheduleId", scheduleId))
        })
        moimDiaryAdapter = MoimDiaryRVAdapter(onItemClick = { scheduleId ->
            startActivity(Intent(requireContext(), MoimDiaryDetailActivity::class.java)
                .putExtra("scheduleId", scheduleId))
        })

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
            val scroller = object : LinearSmoothScroller(binding.diaryCalendarRv.context) {
                override fun getVerticalSnapPreference(): Int { return SNAP_TO_END }
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float { return 0.01f }
            }
            scroller.targetPosition = calendarAdapter.itemCount - 1
            binding.diaryCalendarRv.layoutManager?.startSmoothScroll(scroller)
        }
    }

    private fun setupScrollListener() {
        var scrollJob: Job? = null // 코루틴 Job 변수로 스크롤을 제어

        binding.diaryCalendarRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrollJob?.cancel() // 기존 Job 취소
                    scrollJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(200)
                        setDiaryIndicator(recyclerView)
                        updateVisibleMonth(recyclerView) // 스크롤 멈췄을 때 중앙 달 업데이트
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateReturnBtnVisible()
                if (scrollJob == null || scrollJob?.isActive == false) {
                    scrollJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(300)
                        updateVisibleMonth(recyclerView) // 스크롤 도중에도 중앙 달 업데이트
                    }
                }
            }
        })
    }

    private fun updateVisibleMonth(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        // 중앙에 위치한 아이템 계산
        val centerPosition = (firstVisiblePosition + lastVisiblePosition) / 2
        val centerItem = calendarAdapter.getItemAtPosition(centerPosition)

        // 어댑터에 중앙에 보이는 달 전달
        centerItem?.let {
            val currentMonth = it.toYearMonth()
            if (currentMonth != lastDisplayedMonth) {
                lastDisplayedMonth = currentMonth
                showMonthSnackBar(it.year, it.month + 1) // 스낵바 띄우기
            }
            calendarAdapter.updateVisibleMonth(currentMonth)
        }
    }




    private fun updateReturnBtnVisible() {
        val layoutManager = binding.diaryCalendarRv.layoutManager as GridLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount

        val isAtBottom = lastVisibleItemPosition == totalItemCount - 1
        val isBottomSheetOpened = viewModel.isBottomSheetOpened.value ?: false

        viewModel.setReturnBtnVisible(!isAtBottom && !isBottomSheetOpened)
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
            val yearMonth = calendarDay.toYearMonth()

            // 조회한 적 없는 달인 경우 서버에 요청
            if (!fetchedMonths.contains(yearMonth)) {
                fetchedMonths.add(yearMonth)
                viewModel.getCalendarDiaryData(yearMonth)
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
        viewModel.calendarDiary.observe(viewLifecycleOwner) { calendarDiaryResult ->
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
            set(START_YEAR, Calendar.JANUARY, 1)
        }

        val daysFromStart = ((today.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        binding.diaryCalendarRv.scrollToPosition(daysFromStart)
    }

    override fun onCalendarDayClick(date: CalendarDay) {
        if (viewModel.isBottomSheetOpened.value == true) {
            if (viewModel.selectedDate.value?.isSameDate(date) == true) {
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
}
