package com.example.namo.bottom.home.calendar

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.forEach
import com.example.namo.R
import com.example.namo.bottom.home.calendar.events.Event
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.DAYS_PER_WEEK
import kotlin.math.max

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    @AttrRes defStyleAttr : Int = R.attr.calendarViewStyle,
    @StyleRes defStyleRes : Int = R.style.Calendar_CalendarViewStyle
) : ViewGroup(ContextThemeWrapper(context, defStyleRes), attrs, defStyleAttr) {

//    private var _height : Float = 0f
//
//    init {
//        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
//            _height = getDimension(R.styleable.CalendarView_dayHeight, 0f)
//        }
//    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val iWidth = (width / DAYS_PER_WEEK).toFloat()
        val iHeight = (height / WEEKS_PER_MONTH).toFloat()

        var index = 0
        children.forEach { view ->
            val left = (index % DAYS_PER_WEEK) * iWidth
            val top = (index / DAYS_PER_WEEK) * iHeight

            view.layout(left.toInt(), top.toInt(), (left+iWidth).toInt(), (top+iHeight).toInt())

            index++
        }
    }

    /**
     * 달력 그리기 시작한다.
     * @param firstDayOfMonth   한 달의 시작 요일
     * @param list              달력이 가지고 있는 요일과 이벤트 목록 (총 42개)
     */
    fun initCalendar(firstDayOfMonth : DateTime, list : List<DateTime>, eventList : ArrayList<Event>) {

        list.forEach {

            var events = getTodayEvent(eventList, it)
            events.forEach {
                it.idx = getIndex(it, eventList)
            }

            addView(DayItemView(
                context = context,
                date = it,
                firstDayOfMonth = firstDayOfMonth,
                eventList = events
            ))
        }
    }

    private fun getTodayEvent(eventList : ArrayList<Event>, today : DateTime) : ArrayList<Event> {
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

    private fun getIndex(event : Event, eventList : ArrayList<Event>) : Int {
        var maxIdx = 0
        var idx = 0
        for (i in 0 until event.interval + 1) {
            var temp = getTodayEvent(eventList, DateTime(event.startLong).withTimeAtStartOfDay().plusDays(i))
            idx = temp.indexOf(event)
            if (maxIdx < idx) {
                maxIdx = idx
            }
        }

        return maxIdx
    }

    private fun isEventHaveToday(event : Event, today : DateTime) : Boolean {
        val start = DateTime(event.startLong).withTimeAtStartOfDay()
        val end = DateTime(event.endLong).withTimeAtStartOfDay()
        val now = today.withTimeAtStartOfDay()

        if ((now.isAfter(start) && now.isBefore(end)) || now.isEqual(start) || now.isEqual(end)) {
            return true
        }
        return false
    }
}