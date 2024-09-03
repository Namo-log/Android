package com.mongmong.namo.presentation.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.mongmong.namo.data.local.entity.home.Schedule
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class CalendarUtils {
    companion object {
        const val WEEKS_PER_MONTH = 6
        const val DAYS_PER_WEEK = 7

        /**
         * 선택된 날짜에 해당하는 월간 달력을 반환한다.
         * (이전 달, 다음 달의 날짜도 포함)
         */
        fun getMonthList(dateTime: DateTime): List<DateTime> {
            val list = mutableListOf<DateTime>()

            val date = dateTime.withDayOfMonth(1)
            val prev = getPrevOffset(date)

            val startValue = date.minusDays(prev).withTimeAtStartOfDay()

            val totalDay = DateTimeConstants.DAYS_PER_WEEK * WEEKS_PER_MONTH

            for (i in 0 until totalDay) {
                list.add(DateTime(startValue.plusDays(i)))
            }

            return list
        }

        /**
         * 선택된 날짜에 해당하는 월간 달력을 반환한다.
         * (해당 달의 날짜만 포함)
         */
        fun getDiaryMonthList(dateTime: DateTime): List<DateTime> {
            val startOfMonth = dateTime.withDayOfMonth(1).withTimeAtStartOfDay()
            val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

            val days = mutableListOf<DateTime>()

            // 이전 달 날짜 채우기
            val firstDayOfWeek = startOfMonth.dayOfWeek
            if (firstDayOfWeek != 7) {
                val prevMonthEnd = startOfMonth.minusDays(1)
                for (i in 0 until firstDayOfWeek) {
                    days.add(prevMonthEnd.minusDays(i))
                }
            }
            days.reverse()

            // 현재 달 날짜 추가
            for (i in 0 until endOfMonth.dayOfMonth) {
                days.add(startOfMonth.plusDays(i))
            }

            // 다음 달 날짜 채우기
            val lastDayOfWeek = endOfMonth.dayOfWeek
            if (lastDayOfWeek != 6) {
                val nextMonthStart = endOfMonth.plusDays(1)
                for (i in 0 until (6 - lastDayOfWeek)) {
                    days.add(nextMonthStart.plusDays(i))
                }
            }

            return days
        }



        /**
         * 해당 calendar 의 이전 달의 일 갯수를 반환한다.
         */
        fun getPrevOffset(dateTime: DateTime): Int {
            var prevMonthTailOffset = dateTime.dayOfWeek

            if (prevMonthTailOffset >= 7) prevMonthTailOffset %= 7

            return prevMonthTailOffset
        }

        /**
         * 같은 달인지 체크
         */
        fun isSameMonth(first: DateTime, second: DateTime): Boolean =
            first.year == second.year && first.monthOfYear == second.monthOfYear

        /**
         * 해당 요일의 색깔을 반환한다.
         * 일요일 -> 빨간색
         * 토요일 -> 파란색
         * 나머지 -> 검정색
         */
        @ColorInt
        fun getDateColor(today: Boolean, context: Context): Int {
            return if (today) Color.WHITE
            else Color.BLACK
        }

        fun getInterval(start: Long, end: Long): Int {
            return ((end - start) / (24 * 60 * 60 * 1000)).toInt()
        }

        fun getOrder(event: Schedule, eventList: ArrayList<Schedule>): Int {
            var maxIdx = 0
            var idx = 0
            for (i in 0 until event.dayInterval + 1) {
                val temp = getTodaySchedule(eventList, DateTime(event.startLong).withTimeAtStartOfDay().plusDays(i))
                idx = temp.indexOf(event)
                if (maxIdx < idx) {
                    maxIdx = idx
                }
            }

            return maxIdx
        }

        fun getTodaySchedule(eventList: ArrayList<Schedule>, today: DateTime): ArrayList<Schedule> {
            val contains = ArrayList<Schedule>()

            eventList.forEach {
                if (isScheduleHaveToday(it, today)) {
                    contains.add(it)
                }
            }

            return contains
        }

        fun isScheduleHaveToday(event: Schedule, today: DateTime): Boolean {
            val start = DateTime(event.startLong).withTimeAtStartOfDay()
            val end = DateTime(event.endLong).withTimeAtStartOfDay()
            val now = today.withTimeAtStartOfDay()

            return (now.isAfter(start) && now.isBefore(end)) || now.isEqual(start) || now.isEqual(end)
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
        }
    }
}
