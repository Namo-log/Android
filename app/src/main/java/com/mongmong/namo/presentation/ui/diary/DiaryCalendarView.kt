package com.mongmong.namo.presentation.ui.diary

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.mongmong.namo.R
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.getDiaryMonthList
import com.mongmong.namo.presentation.utils.CustomCalendarView
import org.joda.time.DateTime
import kotlin.math.abs

class DiaryCalendarView(
    context: Context, attrs: AttributeSet
) : CustomCalendarView(context, attrs) {

    private var isBottomSheetOpen = false

    init {
        setInitialScrollToToday()
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("DiaryCalendarView", "onDraw")
        drawDays(canvas)
    }

    override fun drawSchedules(canvas: Canvas) {
        // DiaryCalendarView에서 스케줄을 그리는 로직을 정의합니다.
    }

    private fun setInitialScrollToToday() {
        // 오늘 날짜가 포함된 위치로 스크롤 위치 설정
        val today = DateTime.now().withTimeAtStartOfDay()
        val indexOfToday = days.indexOf(today)
        if (indexOfToday != -1) {
            scrollToPosition(indexOfToday)
        }
    }

    private fun scrollToPosition(position: Int) {
        // 스크롤 위치를 설정하는 로직을 추가
    }

    override fun setDays(millis: Long) {
        this.millis = millis
        days.clear()

        // 현재 달의 첫 번째 날과 마지막 날을 계산
        val startDate = DateTime(millis).withDayOfMonth(1).withTimeAtStartOfDay()
        val endDate = startDate.plusMonths(1).minusDays(1)

        // 현재 달의 날짜 추가
        for (i in 0 until endDate.dayOfMonth) {
            days.add(startDate.plusDays(i))
        }

        // 다음 달의 날짜 추가
        val nextMonthStart = endDate.plusDays(1)
        for (i in 0 until DAYS_PER_WEEK) {
            days.add(nextMonthStart.plusDays(i))
        }

        invalidate()
    }



    override fun drawDays(canvas: Canvas) {
        val padding = dpToPx(context, 5f)
        val today = DateTime.now().withTimeAtStartOfDay()
        val currentMonth = DateTime(millis).monthOfYear

        for (day in days.indices) {
            val dayDate = days[day]

            val x = (day % DAYS_PER_WEEK) * cellWidth + padding
            val y = (day / DAYS_PER_WEEK) * cellHeight

            val dayString = if (dayDate.dayOfMonth == 1 && dayDate.monthOfYear != currentMonth) {
                dayDate.toString("M/d")
            } else {
                dayDate.dayOfMonth.toString()
            }

            // 날짜 색상 설정
            datePaint.color = when {
                dayDate.isEqual(today) -> context.getColor(R.color.main_text)
                dayDate.monthOfYear == currentMonth -> context.getColor(R.color.black)
                else -> context.getColor(R.color.main_text)
            }

            datePaint.getTextBounds(dayString, 0, dayString.length, bounds)
            canvas.drawText(dayString, x, y + _dayTextHeight, datePaint)
        }
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                endX = event.x
                endY = event.y

                isScroll = !(abs(endX - startX) < 10 && abs(endY - startY) < 10)

                if (!isScroll) {
                    handleDateClick(event)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleDateClick(event: MotionEvent) {
        val x = event.x
        val y = event.y

        var foundDate: DateTime? = null
        var foundPos: Int? = null

        for (i in days.indices) {
            val col = i % DAYS_PER_WEEK
            val row = i / DAYS_PER_WEEK
            val xPos = col * cellWidth + dpToPx(context, 5f)
            val yPos = row * cellHeight

            if (x in xPos..(xPos + cellWidth) && y in yPos..(yPos + cellHeight)) {
                foundDate = days[i]
                foundPos = i
                break
            }
        }

        if (foundDate != null && foundPos != null) {
            onDateClickListener?.onDateClick(foundDate, foundPos)
            toggleBottomSheet()
        }
    }

    private fun toggleBottomSheet() {
        isBottomSheetOpen = !isBottomSheetOpen
        if (isBottomSheetOpen) {
            adjustCellHeightForBottomSheetOpen()
        } else {
            adjustCellHeightForBottomSheetClosed()
        }
    }

    private fun adjustCellHeightForBottomSheetOpen() {
        cellHeight = calculateHeightForBottomSheetOpen()
        invalidate()
    }

    private fun adjustCellHeightForBottomSheetClosed() {
        cellHeight = calculateHeightForBottomSheetClosed()
        invalidate()
    }

    private fun calculateHeightForBottomSheetOpen(): Float {
        return cellHeight * 0.7f
    }

    private fun calculateHeightForBottomSheetClosed(): Float {
        return cellHeight * 1.0f
    }
}
