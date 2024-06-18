package com.mongmong.namo.presentation.ui.group.calendar

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.mongmong.namo.presentation.utils.CustomCalendarView
import org.joda.time.DateTime

class GroupCalendarView(context: Context, attrs: AttributeSet) :
    CustomCalendarView(context, attrs) {
    private val scheduleList = mutableListOf<MoimScheduleBody>()

    override fun drawSchedules(canvas: Canvas) {
        if (cellHeight - eventTop > _eventHeight * 4) {
            for (i in 0 until scheduleList.size) {
                val startIdx = days.indexOf(DateTime(scheduleList[i].startDate * 1000L).withTimeAtStartOfDay())
                val endIdx = days.indexOf(DateTime(scheduleList[i].endDate * 1000L).withTimeAtStartOfDay())

                for (splitSchedule in splitWeek(startIdx, endIdx)) {
                    val order = findMaxOrderInSchedule(splitSchedule.startIdx, splitSchedule.endIdx)
                    setOrder(order, splitSchedule.startIdx, splitSchedule.endIdx)

                    if (cellHeight - getScheduleBottom(order) < _eventHeight) {
                        for (idx in splitSchedule.startIdx..splitSchedule.endIdx) {
                            moreList[idx] = moreList[idx] + 1
                        }
                        continue
                    }

                    rect = setRect(order, splitSchedule.startIdx, splitSchedule.endIdx)
                    val path = Path()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    setBgPaintColor(scheduleList[i])
                    canvas.drawPath(path, bgPaint)

                    // 텍스트 너비 계산 및 경로 너비 초과 시 텍스트 잘라내기
                    val textWidth = eventPaint.measureText(scheduleList[i].name) + (2 * _eventHorizontalPadding)
                    val pathWidth = rect.width()
                    val textToDraw = if (textWidth > pathWidth) {
                        val availableWidth = pathWidth - (2 * _eventHorizontalPadding)
                        val truncatedLength = eventPaint.breakText(scheduleList[i].name, true, availableWidth, null)
                        scheduleList[i].name.substring(0, truncatedLength)
                    } else {
                        scheduleList[i].name
                    }

                    eventPaint.getTextBounds(textToDraw, 0, textToDraw.length, eventBounds)

                    // 이모지와 일반 텍스트 모두에 대해 텍스트 높이를 계산하여 중앙 정렬
                    val textHeight = eventBounds.height().toFloat()
                    val textBottom = (rect.top + rect.bottom) / 2 + textHeight / 2 - eventBounds.bottom

                    // 텍스트가 블록 중앙에 오도록 위치 조정
                    canvas.drawText(
                        textToDraw,
                        getScheduleTextStart(splitSchedule.startIdx),
                        textBottom,
                        eventPaint
                    )
                }
            }

            for (more in 0 until 42) {
                if (moreList[more] != 0) {
                    val moreText: String = "+${moreList[more]}"

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
        } else {
            for (i in 0 until scheduleList.size) {
                val startIdx = days.indexOf(DateTime(scheduleList[i].startDate * 1000L).withTimeAtStartOfDay())
                val endIdx = days.indexOf(DateTime(scheduleList[i].endDate * 1000L).withTimeAtStartOfDay())

                for (splitSchedule in splitWeek(startIdx, endIdx)) {
                    val order = findMaxOrderInSchedule(splitSchedule.startIdx, splitSchedule.endIdx)
                    setOrder(order, splitSchedule.startIdx, splitSchedule.endIdx)

                    if (getScheduleLineBottom(order) >= cellHeight) {
                        continue
                    }

                    rect = setLineRect(order, splitSchedule.startIdx, splitSchedule.endIdx)
                    val path = Path()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    setBgPaintColor(scheduleList[i])
                    canvas.drawPath(path, bgPaint)
                }
            }
        }
    }

    private fun setBgPaintColor(event: MoimScheduleBody) {
        //  Log.d("BG_COLOR", event.toString())
        val paletteId = if (event.curMoimSchedule) 4
//                        else {
//                            if (event.users.size < 2 && event.users[0].color != 0) event.users[0].color
//                            else 3
//                        }
        else event.users[0].color
        //    Log.d("GroupCalView", "유저 : ${event.users} | paletteId : ${paletteId}")
        bgPaint.color = Color.parseColor(CategoryColor.getAllColors()[paletteId - 1])
    }

    fun setScheduleList(events: List<MoimScheduleBody>) {
        val sortedEvents = events.sortedWith(compareByDescending<MoimScheduleBody> {
            DateTime(it.endDate * 1000L).millis - DateTime(it.startDate * 1000L).millis
        }.thenBy {
            it.startDate
        })

        scheduleList.clear()
        scheduleList.addAll(sortedEvents)

        invalidate()
    }
}