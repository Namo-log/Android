package com.mongmong.namo.presentation.ui.home.calendar

import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.mongmong.namo.presentation.utils.CustomCalendarView
import org.joda.time.DateTime

class PersonalCalendarView(context: Context, attrs: AttributeSet) :
    CustomCalendarView(context, attrs) {

    private val scheduleList = mutableListOf<Schedule>()

    override fun drawSchedules(canvas: Canvas) {
        if (cellHeight - eventTop > _eventHeight * 3) {
            for (i in 0 until scheduleList.size) {
//                x계산하고, y계산하기
                val startIdx =
                    days.indexOf(DateTime(scheduleList[i].startLong * 1000L).withTimeAtStartOfDay())
                val endIdx =
                    days.indexOf(DateTime(scheduleList[i].endLong * 1000L).withTimeAtStartOfDay())

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
                    canvas!!.drawPath(path, bgPaint)

                    val textWidth =
                        eventPaint.measureText(scheduleList[i].title) + (2 * _eventHorizontalPadding)
                    val pathWidth = rect.width()

                    if (textWidth > pathWidth) {
                        val ellipsizedText = TextUtils.ellipsize(
                            scheduleList[i].title,
                            eventPaint,
                            pathWidth - (2 * _eventHorizontalPadding),
                            TextUtils.TruncateAt.END
                        )

                        eventPaint.getTextBounds(
                            ellipsizedText.toString(),
                            0,
                            ellipsizedText.toString().length,
                            eventBounds
                        )

                        canvas.drawText(
                            ellipsizedText.toString(),
                            getScheduleTextStart(splitSchedule.startIdx),
                            getScheduleTextBottom(
                                ellipsizedText.toString(),
                                splitSchedule.startIdx,
                                splitSchedule.endIdx,
                                order
                            ),
                            eventPaint
                        )

                    } else {
                        eventPaint.getTextBounds(
                            scheduleList[i].title,
                            0,
                            scheduleList[i].title.length,
                            eventBounds
                        )

                        canvas.drawText(
                            scheduleList[i].title,
                            getScheduleTextStart(splitSchedule.startIdx),
                            getScheduleTextBottom(
                                scheduleList[i].title,
                                splitSchedule.startIdx,
                                splitSchedule.endIdx,
                                order
                            ),
                            eventPaint
                        )
                    }
                }
            }

            for (more in 0 until 42) {
                if (moreList[more] != 0) {
                    var moreText = "+${moreList[more]}"

                    val x = (more % DAYS_PER_WEEK) * cellWidth
                    val y = (more / DAYS_PER_WEEK + 1) * cellHeight - _eventMorePadding

                    morePaint.getTextBounds(moreText, 0, moreText.length, moreBounds)
                    canvas!!.drawText(
                        moreText,
                        (x + cellWidth / 2 - moreBounds.right.toFloat() / 2),
                        y,
                        morePaint
                    )
                }
            }
        } else {
            for (i in 0 until scheduleList.size) {
//                x계산하고, y계산하기
                val startIdx =
                    days.indexOf(DateTime(scheduleList[i].startLong * 1000L).withTimeAtStartOfDay())
                val endIdx =
                    days.indexOf(DateTime(scheduleList[i].endLong * 1000L).withTimeAtStartOfDay())

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
                    canvas!!.drawPath(path, bgPaint)
                }
            }
        }
    }

    fun setScheduleList(events: List<Schedule>) {
        val sortedEvents = events.sortedWith(compareByDescending<Schedule> {
            DateTime(it.endLong * 1000L).millis - DateTime(it.startLong * 1000L).millis
        }.thenBy {
            it.startLong
        })

        scheduleList.clear()
        scheduleList.addAll(sortedEvents)

        invalidate()
    }


    fun setCategoryList(category: List<Category>) {
        categoryList.clear()
        categoryList.addAll(category)
    }

    private fun setBgPaintColor(event: Schedule) {
        //Log.d("BG_COLOR_CHECK", categoryList.toString())
        //Log.d("BG_COLOR_CHECK", event.toString())
        //Log.d("BG_COLOR_CHECK", "Category Server : " + event.categoryServerId + " | Category Idx : " + event.categoryId)
        val foundCategory = categoryList.find {
            if (it.serverId != 0L) it.serverId == event.categoryServerId
            else it.categoryId == event.categoryId
        }
        val hexColor = CategoryColor.convertPaletteIdToHexColor(foundCategory?.paletteId ?: 0)
        bgPaint.color = Color.parseColor(hexColor)
    }
}