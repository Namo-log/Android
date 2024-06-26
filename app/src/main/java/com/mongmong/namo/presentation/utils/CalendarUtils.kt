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
         */
        fun getMonthList(dateTime: DateTime) : List<DateTime> {
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
         * 해당 calendar 의 이전 달의 일 갯수를 반환한다.
         */
        fun getPrevOffset(dateTime: DateTime) : Int {
            var prevMonthTailOffset = dateTime.dayOfWeek

            if (prevMonthTailOffset >= 7) prevMonthTailOffset %= 7

            return prevMonthTailOffset
        }

        /**
         * 같은 달인지 체크
         */
        fun isSameMonth(first : DateTime, second : DateTime) : Boolean =
            first.year == second.year && first.monthOfYear == second.monthOfYear

        /**
         * 해당 요일의 색깔을 반환한다.
         * 일요일 -> 빨간색
         * 토요일 -> 파란색
         * 나머지 -> 검정색
         */
        @ColorInt
        fun getDateColor(today : Boolean, context : Context) : Int {
            return if(today) Color.WHITE
                    else Color.BLACK
        }
//        @ColorInt
//        fun getDateColor(@IntRange(from = 1, to = 7) dayOfWeek: Int) : Int {
//            return when(dayOfWeek) {
//                DateTimeConstants.SATURDAY -> Color.BLACK
//                DateTimeConstants.SUNDAY -> Color.BLACK
//                else -> Color.BLACK
//            }
//        }

        fun getInterval(start : Long, end : Long) : Int {
            return ((end - start) / (24*60*60*1000)).toInt()
        }

        fun getOrder(event : Schedule, eventList : ArrayList<Schedule>) : Int {
            var maxIdx = 0
            var idx = 0
            for (i in 0 until event.dayInterval + 1) {
                var temp = getTodaySchedule(eventList, DateTime(event.startLong).withTimeAtStartOfDay().plusDays(i))
                idx = temp.indexOf(event)
                if (maxIdx < idx) {
                    maxIdx = idx
                }
            }

            return maxIdx
        }

        fun getTodaySchedule(eventList : ArrayList<Schedule>, today : DateTime) : ArrayList<Schedule> {
            var contains = ArrayList<Schedule>()

            eventList.forEach {
                if (isScheduleHaveToday(it, today)) {
                    contains.add(it)
                }
            }

//        contains.forEach {
//            it.idx = getIndex(it, eventList)
//        }

            return contains
        }

        fun isScheduleHaveToday(event : Schedule, today : DateTime) : Boolean {
            val start = DateTime(event.startLong).withTimeAtStartOfDay()
            val end = DateTime(event.endLong).withTimeAtStartOfDay()
            val now = today.withTimeAtStartOfDay()

            if ((now.isAfter(start) && now.isBefore(end)) || now.isEqual(start) || now.isEqual(end)) {
                return true
            }
            return false
        }

        fun dpToPx(context: Context, dp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics)
        }
    }
}