package com.gradu.presentation.utils

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.gradu.domain.model.Schedule
import kotlinx.datetime.*

class CalendarUtils {
    companion object {
        const val WEEKS_PER_MONTH = 6
        const val DAYS_PER_WEEK = 7

        /**
         * 선택된 날짜에 해당하는 월간 달력을 반환한다.
         * (이전 달, 다음 달의 날짜도 포함)
         */
        fun getMonthList(dateTime: LocalDateTime): List<LocalDateTime> {
            val list = mutableListOf<LocalDateTime>()

            val firstOfMonth = LocalDate(dateTime.year, dateTime.monthNumber, 1)
            val dayOfWeek = firstOfMonth.dayOfWeek.isoDayNumber % 7
            val startDate = firstOfMonth.minus(dayOfWeek, DateTimeUnit.DAY)

            val totalDays = DAYS_PER_WEEK * WEEKS_PER_MONTH
            for (i in 0 until totalDays) {
                val current = startDate.plus(i, DateTimeUnit.DAY)
                list.add(LocalDateTime(current.year, current.monthNumber, current.dayOfMonth, 0, 0))
            }

            return list
        }


        /**
         * 해당 calendar 의 이전 달의 일 갯수를 반환한다.
         */
        fun getPrevOffset(dateTime: LocalDateTime): Int {
            return dateTime.date.dayOfWeek.isoDayNumber % 7
        }

        /**
         * 같은 달인지 체크
         */
        fun isSameMonth(first: LocalDateTime, second: LocalDateTime): Boolean =
            first.year == second.year && first.monthNumber == second.monthNumber

        /**
         * 해당 요일의 색깔을 반환한다.
         * 일요일 -> 빨간색
         * 토요일 -> 파란색
         * 나머지 -> 검정색
         */
        @ColorInt
        fun getDateColor(today: Boolean, context: Context): Int {
            return if (today) Color.WHITE else Color.BLACK
        }

        fun getInterval(start: Long, end: Long): Int {
            return ((end - start) / (24 * 60 * 60 * 1000)).toInt()
        }

        fun getOrder(event: Schedule, eventList: ArrayList<Schedule>): Int {
            var maxIdx = 0
            var idx = 0
            return maxIdx
        }

        fun getTodaySchedule(eventList: ArrayList<Schedule>, today: LocalDateTime): ArrayList<Schedule> {
            val contains = ArrayList<Schedule>()
            eventList.forEach {
                if (isScheduleHaveToday(it, today)) {
                    contains.add(it)
                }
            }
            return contains
        }

        fun isScheduleHaveToday(event: Schedule, today: LocalDateTime): Boolean {
            val start = event.period.startDate.date
            val end = event.period.endDate.date
            val now = today.date
            return (now > start && now < end) || now == start || now == end
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
        }

        fun LocalDateTime.toMillis(): Long {
            return this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        }

        fun millisToLocalDateTime(millis: Long): LocalDateTime {
            return Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
        }

        fun getDateDifferenceInDays(start: kotlinx.datetime.LocalDateTime, end: kotlinx.datetime.LocalDateTime): Int {
            val startDate = start.date
            val endDate = end.date
            return endDate.daysUntil(startDate).let { -it } // 양수로 정렬하려면 부호 반전
        }
    }
}
