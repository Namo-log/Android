package com.example.namo.ui.bottom.home.calendar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.format.DateUtils.isToday
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.namo.R
import com.example.namo.data.entity.home.Event
import com.example.namo.utils.CalendarUtils
import com.example.namo.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.example.namo.utils.CalendarUtils.Companion.dpToPx
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import com.example.namo.utils.CalendarUtils.Companion.spToPx
import org.joda.time.DateTime
import kotlin.math.abs

class Calendar2View(context: Context, attrs : AttributeSet) : View(context, attrs) {

    interface OnDateClickListener {
        fun onDateClick(date : DateTime?, pos : Int?)
    }
    var onDateClickListener : OnDateClickListener? = null
    var selectedDate : DateTime? = null
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isScroll = false

    private var cellWidth = 0
    private var cellHeight = 0
    private val bounds = Rect()
    private val today = DateTime.now().withTimeAtStartOfDay().millis

    private val dayList = mutableListOf<DateTime>()
    private val eventList = mutableListOf<Event>()
    private val orderList = mutableListOf<Int>()

    private val bounds2 = Rect()
    private val eventBounds = Rect()
    private val moreBounds = Rect()
    private var showTitle: Boolean = false
    private var cellPaint: Paint = Paint()
    private var datePaint: Paint = Paint()
    private var todayPaint: Paint = Paint()
    private var selectedPaint : Paint = Paint()
    private var clickPaint: Paint = Paint()
    private var bgPaint: Paint = Paint()
    private var eventPaint: Paint = Paint()
    private var morePaint : Paint = Paint()

    private var todayNoticePaint : Paint = Paint()
    private var radius : Float = 34f

    private var path = Path()
    private var rect = RectF()
    private var corners: FloatArray = floatArrayOf()
    private var eventPos : Int = 0
    private var eventTop : Float = 0f
    private var _dayTextHeight : Float = 0f
    private var _eventHeight: Float = 0f
    private var _eventTopPadding: Float = 0f
    private var _eventMorePadding: Float = 0f
    private var _eventBetweenPadding: Float = 0f
    private var _eventHorizontalPadding: Float = 0f
    private var _eventCornerRadius : Float = 0f
    private var _eventLineHeight : Float = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr = R.attr.itemViewStyle, defStyleRes = R.style.Calendar_ItemViewStyle) {
            val dayTextSize = getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()
            val eventTextSize = getDimensionPixelSize(R.styleable.CalendarView_eventTextSize, 0).toFloat()
            _dayTextHeight = getDimension(R.styleable.CalendarView_dayTextHeight, 0f)
            _eventHeight = getDimension(R.styleable.CalendarView_eventHeight, 0f)
            _eventTopPadding = getDimension(R.styleable.CalendarView_eventTopPadding, 0f)
            _eventMorePadding = getDimension(R.styleable.CalendarView_eventMorePadding, 0f)
            _eventBetweenPadding = getDimension(R.styleable.CalendarView_eventBetweenPadding, 0f)
            _eventHorizontalPadding = getDimension(R.styleable.CalendarView_eventHorizontalPadding, 0f)
            _eventCornerRadius = getDimension(R.styleable.CalendarView_eventCornerRadius, 0f)
            _eventLineHeight = getDimension(R.styleable.CalendarView_eventLineHeight, 0f)

            cellPaint = Paint().apply {
                style = Paint.Style.STROKE
                color = Color.BLACK
                strokeWidth = dpToPx(context, 0.5f)
                isAntiAlias = true
            }

            datePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = resources.getColor(R.color.black)
            }

            todayPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = resources.getColor(R.color.white)
            }

            todayNoticePaint.color = resources.getColor(R.color.MainOrange)

            selectedPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = resources.getColor(R.color.MainOrange)
            }

            bgPaint.apply {
                color = resources.getColor(R.color.palette3)
//                if (!CalendarUtils.isSameMonth(date, firstDayOfMonth)) {
//                    alpha = 50
//                }
            }

            eventPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = eventTextSize
                color = Color.WHITE
                typeface = Typeface.DEFAULT_BOLD
//                if (!isSameMonth(date, firstDayOfMonth)) {
//                    alpha = 50
//                }
            }

            corners = floatArrayOf(
                _eventCornerRadius, _eventCornerRadius, //Top left
                _eventCornerRadius, _eventCornerRadius, //Top right
                _eventCornerRadius, _eventCornerRadius, //Bottom right
                _eventCornerRadius, _eventCornerRadius //Bottom left
            )
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        cellWidth = width / DAYS_PER_WEEK
        cellHeight = height / WEEKS_PER_MONTH
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (day in 0 until 42) {
            val x = (day % DAYS_PER_WEEK) * cellWidth
            val y = (day / DAYS_PER_WEEK) * cellHeight

            if (dayList[day].isEqual(today)) {
                todayPaint.getTextBounds(dayList[day].dayOfMonth.toString(), 0, dayList[day].dayOfMonth.toString().length, bounds)
                canvas!!.drawCircle((x + cellWidth / 2).toFloat(), (y + _dayTextHeight - bounds.height() / 2), bounds.height().toFloat(), todayNoticePaint)
                canvas!!.drawText(dayList[day].dayOfMonth.toString(), (x + cellWidth / 2 - bounds.width() / 2).toFloat(), y + _dayTextHeight, todayPaint)
//                Log.d("DATE_CHECK", "날짜 ${dayList[day].dayOfMonth}, paint : todayPaint")
            } else {
                datePaint.getTextBounds(dayList[day].dayOfMonth.toString(), 0, dayList[day].dayOfMonth.toString().length, bounds)
                canvas!!.drawText(dayList[day].dayOfMonth.toString(), (x + cellWidth / 2 - bounds.width() / 2).toFloat(), y + _dayTextHeight, datePaint)
//                Log.d("DATE_CHECK", "날짜 ${dayList[day].dayOfMonth}, paint : datePaint")
            }

        }

        if (selectedDate != null) {
            val selectedDay = selectedDate!!.dayOfMonth
            for (i in dayList.indices) {
                if (dayList[i] == selectedDate && !dayList[i].isEqual(today)) {
                    val x = (i % DAYS_PER_WEEK) * cellWidth
                    val y = (i / DAYS_PER_WEEK) * cellHeight

                    selectedPaint.getTextBounds(selectedDay.toString(), 0, selectedDay.toString().length, bounds)
                    canvas!!.drawText(selectedDay.toString(), (x + cellWidth / 2 - bounds.width() / 2).toFloat(), y + _dayTextHeight, selectedPaint)
                    break
                }
            }
        }

        eventTop = _dayTextHeight + _eventTopPadding

        orderList.clear()
        for (i in 0 until 42) orderList.add(0)

        if (cellHeight - eventTop > _eventHeight * 3) {
            for (i in 0 until eventList.size) {
//                x계산하고, y계산하기

                val startIdx = dayList.indexOf(DateTime(eventList[i].startLong).withTimeAtStartOfDay())
                val endIdx = dayList.indexOf(DateTime(eventList[i].endLong).withTimeAtStartOfDay())
                Log.d("CHECK_EVENT", "start idx : $startIdx | end idx : $endIdx")

                if (findWeek(startIdx) != findWeek(endIdx)) {
//                    시작일이랑 끝일이 여러 주에 걸쳐있음
                }
                else {
//                    시작일이랑 끝일이 같은 주에 있음
                    Log.d("CHECK_WEEK", "Start drawing same week")
                    val order = findMaxOrderInWeek(findWeek(startIdx))!!
                    Log.d("CHECK_ORDER", order.toString())
                    setOrder(order, startIdx, endIdx)
                    rect = setRect(order, startIdx, endIdx)
                    Log.d("CHECK_RECT", rect.toString())
                    val path = Path()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    setBgPaintColor(eventList[i])
                    canvas!!.drawPath(path, bgPaint)

                    eventPaint.getTextBounds(eventList[i].title, 0, eventList[i].title.length, eventBounds)
                    canvas!!.drawText(
                        eventList[i].title,
                        getEventTextStart(eventList[i].title, startIdx, endIdx),
                        getEventTextBottom(eventList[i].title, startIdx, endIdx, order),
                        eventPaint
                    )
                }

            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
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
                    val row = (event.y / cellHeight).toInt()
                    val col = (event.x / cellWidth).toInt()
                    if (row in 0..5 && col in 0..6) {
                        val day = dayList[row * 7 + col]
                        Log.d("TOUCH_DAY", (row * 7 + col).toString())
                        onDateClickListener?.onDateClick(day, row * 7 + col)
                        return true
                    }
                }
                else onDateClickListener?.onDateClick(null, null)
            }
        }

        return super.onTouchEvent(event)
    }

    private fun findWeek(idx : Int) : Int {
        return when(idx) {
            0, 1, 2, 3, 4, 5, 6 -> 0
            7, 8, 9, 10, 11, 12, 13 -> 1
            14, 15, 16, 17, 18, 19, 20 -> 2
            21, 22, 23, 24, 25, 26, 27 -> 3
            28, 29, 30, 31, 32, 33, 34 -> 4
            35, 36, 37, 38, 39, 40, 41 -> 5
            else -> 6
        }
    }

    private fun findMaxOrderInWeek(week : Int) : Int? {
        val arr = arrayOf(
            orderList[week*7],
            orderList[week*7+1],
            orderList[week*7+2],
            orderList[week*7+3],
            orderList[week*7+4],
            orderList[week*7+5],
            orderList[week*7+6]
        )

        return arr.maxOrNull()
    }

    private fun setOrder(order : Int, startIdx : Int, endIdx : Int) {
        for (i in startIdx .. endIdx) {
            orderList[i] = order + 1
        }
    }

    private fun setRect(order : Int, startIdx : Int, endIdx : Int) : RectF {
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1))
        )
    }

    private fun setBgPaintColor(event: Event) {
        bgPaint.color = resources.getColor(event.categoryColor)
//        if (!CalendarUtils.isSameMonth(date, firstDayOfMonth)) {
//            bgPaint.alpha = 50
//        }
    }

    private fun getEventTextStart(title : String, startIdx : Int, endIdx : Int) : Float {
        return (((startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding) + ((endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding)) / 2 - eventBounds.width() / 2
    }

    private fun getEventTextBottom(title : String, startIdx : Int, endIdx : Int, order : Int) : Float {
        return (((startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order) + ((endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1)))) / 2 + eventBounds.height() / 2
    }

    fun setDayList(millis : Long) {
        dayList.clear()
        dayList.addAll(getMonthList(DateTime(millis)))
        orderList.clear()
        for (i in 0 until 42) orderList.add(0)
//        Log.d("ORDER_CHECK", orderList.toString())
//        Log.d("ORDER_CHECK", orderList.size.toString())
//        Log.d("ORDER_CHECK", orderList[0].toString())
//        invalidate()
    }

    fun setEventList(events : List<Event>) {
        eventList.clear()
        eventList.addAll(events)
        invalidate()
    }

    fun getDayList() : MutableList<DateTime> {
        return dayList
    }

    fun getEventList() : MutableList<Event> {
        return eventList
    }
}