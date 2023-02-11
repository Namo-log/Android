package com.example.namo.utils

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import com.example.namo.data.entity.home.calendar.Event
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class CalendarUtils {
    companion object {
        const val WEEKS_PER_MONTH = 6

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

        fun getOrder(event : Event, eventList : ArrayList<Event>) : Int {
            var maxIdx = 0
            var idx = 0
            for (i in 0 until event.dayInterval + 1) {
                var temp = getTodayEvent(eventList, DateTime(event.startLong).withTimeAtStartOfDay().plusDays(i))
                idx = temp.indexOf(event)
                if (maxIdx < idx) {
                    maxIdx = idx
                }
            }

            return maxIdx
        }

        fun getTodayEvent(eventList : ArrayList<Event>, today : DateTime) : ArrayList<Event> {
            var contains = ArrayList<Event>()

            eventList.forEach {
                if (isEventHaveToday(it, today)) {
                    contains.add(it)
                }
            }

//        contains.forEach {
//            it.idx = getIndex(it, eventList)
//        }

            return contains
        }

        fun isEventHaveToday(event : Event, today : DateTime) : Boolean {
            val start = DateTime(event.startLong).withTimeAtStartOfDay()
            val end = DateTime(event.endLong).withTimeAtStartOfDay()
            val now = today.withTimeAtStartOfDay()

            if ((now.isAfter(start) && now.isBefore(end)) || now.isEqual(start) || now.isEqual(end)) {
                return true
            }
            return false
        }
    }
}