package com.gradu.presentation.ui.community.calendar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.gradu.presentation.ui.home.calendar.data.StartEnd
import com.gradu.core.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.gradu.core.utils.converter.ScheduleDateConverter.parseLocalDateTimeToDateTime
import com.gradu.presentation.ui.common.CustomCalendarView
import org.joda.time.Days

class CommunityCalendarView(context: Context, attrs: AttributeSet) :
    CustomCalendarView(context, attrs) {

    private var isFriendCalendar: Boolean = false
    private val scheduleList = mutableListOf<CommunityCommonSchedule>()

    override fun drawSchedules(canvas: Canvas) {
        if (cellHeight - eventTop > _eventHeight * 4) {
            drawDetailedSchedules(canvas)
        } else {
            drawCompactSchedules(canvas)
        }

        drawMoreText(canvas)
    }

    private fun drawDetailedSchedules(canvas: Canvas) {
        for (i in scheduleList.indices) {
            val startIdx = days.indexOf(parseLocalDateTimeToDateTime(scheduleList[i].startDate).withTimeAtStartOfDay())
            val endIdx = days.indexOf(parseLocalDateTimeToDateTime(scheduleList[i].endDate).withTimeAtStartOfDay())

            if (startIdx == -1 || endIdx == -1) continue // 해당 달에 속하지 않는 일정은 무시

            for (splitSchedule in splitWeek(startIdx, endIdx)) {
                val order = findMaxOrderInSchedule(splitSchedule.startIdx, splitSchedule.endIdx)
                setOrder(order, splitSchedule.startIdx, splitSchedule.endIdx)

                if (cellHeight - getScheduleBottom(order) < _eventHeight) {
                    incrementMoreList(splitSchedule)
                    continue
                }

                drawScheduleRect(canvas, scheduleList[i], order, splitSchedule)
            }
        }
    }

    private fun drawCompactSchedules(canvas: Canvas) {
        for (i in scheduleList.indices) {
            val startIdx = days.indexOf(parseLocalDateTimeToDateTime(scheduleList[i].startDate).withTimeAtStartOfDay())
            val endIdx = days.indexOf(parseLocalDateTimeToDateTime(scheduleList[i].endDate).withTimeAtStartOfDay())

            if (startIdx == -1 || endIdx == -1) continue // 해당 달에 속하지 않는 일정은 무시

            for (splitSchedule in splitWeek(startIdx, endIdx)) {
                val order = findMaxOrderInSchedule(splitSchedule.startIdx, splitSchedule.endIdx)
                setOrder(order, splitSchedule.startIdx, splitSchedule.endIdx)

                if (getScheduleLineBottom(order) >= cellHeight) {
                    continue
                }

                drawScheduleLine(canvas, scheduleList[i], order, splitSchedule)
            }
        }
    }

    private fun drawScheduleRect(canvas: Canvas, schedule: CommunityCommonSchedule, order: Int, splitSchedule: StartEnd) {
        rect = setRect(order, splitSchedule.startIdx, splitSchedule.endIdx)
        val path = Path().apply {
            addRoundRect(rect, corners, Path.Direction.CW)
        }
        setBgPaintColor(schedule)
        canvas.drawPath(path, bgPaint)

        val textToDraw = getTruncatedText(schedule.title, rect.width())
        drawScheduleText(canvas, textToDraw, splitSchedule.startIdx, rect)
    }

    private fun drawScheduleLine(canvas: Canvas, schedule: CommunityCommonSchedule, order: Int, splitSchedule: StartEnd) {
        rect = setLineRect(order, splitSchedule.startIdx, splitSchedule.endIdx)
        val path = Path().apply {
            addRoundRect(rect, corners, Path.Direction.CW)
        }
        setBgPaintColor(schedule)
        canvas.drawPath(path, bgPaint)
    }

    private fun incrementMoreList(splitSchedule: StartEnd) {
        for (idx in splitSchedule.startIdx..splitSchedule.endIdx) {
            moreList[idx] += 1
        }
    }

    private fun drawMoreText(canvas: Canvas) {
        for (more in 0 until 42) {
            if (moreList[more] != 0) {
                val moreText = "+${moreList[more]}"
                val x = (more % DAYS_PER_WEEK) * cellWidth
                val y = (more / DAYS_PER_WEEK + 1) * cellHeight - _eventMorePadding

                morePaint.getTextBounds(moreText, 0, moreText.length, moreBounds)
                canvas.drawText(
                    moreText,
                    (x + cellWidth / 2 - moreBounds.right.toFloat() / 2),
                    y,
                    morePaint
                )
            }
        }
    }

    private fun getTruncatedText(text: String, availableWidth: Float): String {
        val textWidth = eventPaint.measureText(text) + (2 * _eventHorizontalPadding)
        return if (textWidth > availableWidth) {
            val limitLength = eventPaint.breakText(text, true, availableWidth - (2 * _eventHorizontalPadding), null)
            text.substring(0, limitLength)
        } else {
            text
        }
    }

    private fun drawScheduleText(canvas: Canvas, text: String, startIdx: Int, rect: RectF) {
        eventPaint.getTextBounds(text, 0, text.length, eventBounds)
        val textHeight = eventBounds.height().toFloat()
        val textBottom = (rect.top + rect.bottom) / 2 + textHeight / 2 - eventBounds.bottom

        canvas.drawText(
            text,
            getScheduleTextStart(startIdx),
            textBottom,
            eventPaint
        )
    }

    fun setCalendarType(isFriendCalendar: Boolean) {
        this.isFriendCalendar = isFriendCalendar
    }

    fun setScheduleList(events: List<CommunityCommonSchedule>) {
        val sortedEvents = events.sortedWith(compareByDescending<CommunityCommonSchedule> {
            Days.daysBetween(it.startDate, it.endDate)
        }.thenBy {
            it.startDate
        })

        scheduleList.clear()
        scheduleList.addAll(sortedEvents)

        invalidate()
    }

    private fun setBgPaintColor(schedule: CommunityCommonSchedule) {
        val colorId = if (isFriendCalendar) schedule.categoryInfo!!.colorId else schedule.participants?.get(0)!!.colorId
        val hexColor = CategoryColor.convertColorIdToHexColor(colorId)
        bgPaint.color = Color.parseColor(hexColor)
    }
}
