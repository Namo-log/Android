package com.example.namo.ui.bottom.home.calendar

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import com.example.namo.R
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.ui.bottom.home.HomeFragment
import com.example.namo.ui.bottom.home.calendar.data.StartEnd
import com.example.namo.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.example.namo.utils.CalendarUtils.Companion.dpToPx
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import org.joda.time.DateTime
import kotlin.math.abs

class CustomCalendarView(context: Context, attrs : AttributeSet) : View(context, attrs) {

    interface OnDateClickListener {
        fun onDateClick(date : DateTime?, pos : Int?)
    }
    var onDateClickListener : OnDateClickListener? = null
    var selectedDate : DateTime? = null
    var millis : Long = 0
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isScroll = false

    private var cellWidth = 0f
    private var cellHeight = 0f
    private val bounds = Rect()
    private val today = DateTime.now().withTimeAtStartOfDay().millis

    private val dayList = mutableListOf<DateTime>()
    private val eventList = mutableListOf<Event>()
    private val categoryList = mutableListOf<Category>()
    private val orderList = mutableListOf<Int>()
    private val moreList = mutableListOf<Int>()
    private val otherRect : ArrayList<RectF> = arrayListOf()

    private val bounds2 = Rect()
    private val eventBounds = Rect()
    private val moreBounds = Rect()
    private var showTitle: Boolean = false
    private var cellPaint: Paint = Paint()
    private var alphaPaint: Paint = Paint()
    private var datePaint: Paint = Paint()
    private var todayPaint: Paint = Paint()
    private var selectedPaint : Paint = Paint()
    private var clickPaint: Paint = Paint()
    private var bgPaint: Paint = Paint()
    private var eventPaint: TextPaint = TextPaint()
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

            alphaPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.WHITE
                alpha = 180
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

            morePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = eventTextSize
                color = Color.BLACK
                typeface = Typeface.DEFAULT_BOLD
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

        cellWidth = width.toFloat() / DAYS_PER_WEEK
        cellHeight = height.toFloat() / WEEKS_PER_MONTH
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (day in 0 until 42) {
            val x = (day % DAYS_PER_WEEK) * cellWidth
            val y = (day / DAYS_PER_WEEK) * cellHeight

            if (dayList[day].isEqual(today)) {
                todayPaint.getTextBounds(dayList[day].dayOfMonth.toString(), 0, dayList[day].dayOfMonth.toString().length, bounds)
                canvas!!.drawCircle((x + cellWidth / 2).toFloat(), (y + _dayTextHeight - bounds.height() / 2), bounds.height().toFloat(), todayNoticePaint)
                canvas!!.drawText(dayList[day].dayOfMonth.toString(), (x + cellWidth / 2 - bounds.right.toFloat() / 2), y + _dayTextHeight, todayPaint)
            } else {
                datePaint.getTextBounds(dayList[day].dayOfMonth.toString(), 0, dayList[day].dayOfMonth.toString().length, bounds)
                canvas!!.drawText(dayList[day].dayOfMonth.toString(), (x + cellWidth / 2 - bounds.right.toFloat() / 2), y + _dayTextHeight, datePaint)
            }

        }

        if (selectedDate != null) {
            val selectedDay = selectedDate!!.dayOfMonth
            for (i in dayList.indices) {
                if (dayList[i] == selectedDate && !dayList[i].isEqual(today)) {
                    val x = (i % DAYS_PER_WEEK) * cellWidth
                    val y = (i / DAYS_PER_WEEK) * cellHeight

                    selectedPaint.getTextBounds(selectedDay.toString(), 0, selectedDay.toString().length, bounds)
                    canvas!!.drawText(selectedDay.toString(), (x + cellWidth / 2 - bounds.right.toFloat() / 2), y + _dayTextHeight, selectedPaint)
                    break
                }
            }
        }

        eventTop = _dayTextHeight + _eventTopPadding

        orderList.clear()
        for (i in 0 until 42) orderList.add(0)
        moreList.clear()
        for (i in 0 until 42) moreList.add(0)

        if (cellHeight - eventTop > _eventHeight * 3) {
            for (i in 0 until eventList.size) {
//                x계산하고, y계산하기
                val startIdx = dayList.indexOf(DateTime(eventList[i].startLong * 1000L).withTimeAtStartOfDay())
                val endIdx = dayList.indexOf(DateTime(eventList[i].endLong * 1000L).withTimeAtStartOfDay())

                for (splitEvent in splitWeek(startIdx, endIdx)) {
                    val order = findMaxOrderInEvent(splitEvent.startIdx, splitEvent.endIdx)
                    setOrder(order, splitEvent.startIdx, splitEvent.endIdx)

                    if (cellHeight - getEventBottom(order) < _eventHeight) {
                        for (idx in splitEvent.startIdx .. splitEvent.endIdx) {
                            moreList[idx] = moreList[idx] + 1
                        }
                        continue
                    }

                    rect = setRect(order, splitEvent.startIdx, splitEvent.endIdx)
                    val path = Path()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    setBgPaintColor(eventList[i])
                    canvas!!.drawPath(path, bgPaint)

                    val textWidth = eventPaint.measureText(eventList[i].title) + (2 * _eventHorizontalPadding)
                    val pathWidth = rect.width()

                    if (textWidth > pathWidth) {
                        val ellipsizedText = TextUtils.ellipsize(eventList[i].title, eventPaint, pathWidth - (2 * _eventHorizontalPadding), TextUtils.TruncateAt.END)

                        eventPaint.getTextBounds(ellipsizedText.toString(), 0, ellipsizedText.toString().length, eventBounds)

                        canvas.drawText(
                            ellipsizedText.toString(),
                            getEventTextStart(ellipsizedText.toString(), splitEvent.startIdx, splitEvent.endIdx),
                            getEventTextBottom(ellipsizedText.toString(), splitEvent.startIdx, splitEvent.endIdx, order),
                            eventPaint
                        )

                    } else {
                        eventPaint.getTextBounds(eventList[i].title, 0, eventList[i].title.length, eventBounds)

                        canvas.drawText(
                            eventList[i].title,
                            getEventTextStart(eventList[i].title, splitEvent.startIdx, splitEvent.endIdx),
                            getEventTextBottom(eventList[i].title, splitEvent.startIdx, splitEvent.endIdx, order),
                            eventPaint
                        )
                    }
                }
            }

            for (more in 0 until 42) {
                if (moreList[more] != 0) {
                    var moreText : String = "+${moreList[more]}"

                    val x = (more % DAYS_PER_WEEK) * cellWidth
                    val y = (more / DAYS_PER_WEEK + 1) * cellHeight - _eventMorePadding

                    morePaint.getTextBounds(moreText, 0, moreText.length, moreBounds)
                    canvas!!.drawText(moreText, (x + cellWidth / 2 - moreBounds.right.toFloat() / 2), y, morePaint)
                }
            }
        }
        else {
            for (i in 0 until eventList.size) {
//                x계산하고, y계산하기
                val startIdx = dayList.indexOf(DateTime(eventList[i].startLong * 1000L).withTimeAtStartOfDay())
                val endIdx = dayList.indexOf(DateTime(eventList[i].endLong * 1000L).withTimeAtStartOfDay())

                for (splitEvent in splitWeek(startIdx, endIdx)) {
                    val order = findMaxOrderInEvent(splitEvent.startIdx, splitEvent.endIdx)
                    setOrder(order, splitEvent.startIdx, splitEvent.endIdx)

                    if (getEventLineBottom(order) >= cellHeight) {
                        continue
                    }

                    rect = setLineRect(order, splitEvent.startIdx, splitEvent.endIdx)
                    val path = Path()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    setBgPaintColor(eventList[i])
                    canvas!!.drawPath(path, bgPaint)
                }
            }
        }

//        이전달, 다음달은 불투명하게
        var prev = 0
        var next = 0
        while (!isSameMonth(dayList[prev])) prev++
        while (!isSameMonth(dayList[41-next])) next++
        drawPrevMonthRect(prev, canvas)
        drawNextMonthRect(next, canvas)
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
                        onDateClickListener?.onDateClick(day, row * 7 + col)
                        return true
                    }
                }
//                else onDateClickListener?.onDateClick(null, null)
            }
        }

        return super.onTouchEvent(event)
    }

    private fun splitWeek(startIdx: Int, endIdx: Int) : ArrayList<StartEnd> {
        val result  = ArrayList<StartEnd>()
        result.clear()
        var start = if (startIdx == -1) 0 else startIdx
        var end = if (endIdx == -1) 41 else endIdx
        var mid = 0

        while (start <= end) {
            mid = (start / 7) * 7 + 6
            if (mid > end) mid = end
            result.add(StartEnd(start, mid))
            start = mid + 1
        }


        return result
    }

    private fun findMaxOrderInEvent(startIdx: Int, endIdx: Int) : Int {
        var maxOrder : Int = 0
        for (i in startIdx ..endIdx) {
            if (orderList[i] > maxOrder) maxOrder = orderList[i]
        }

        return maxOrder
    }

    private fun setOrder(order : Int, startIdx : Int, endIdx : Int) {
        for (i in startIdx .. endIdx) {
            orderList[i] = order + 1
        }
    }

    private fun getEventBottom(idx : Int) : Float {
        return (eventTop + (_eventBetweenPadding * idx) + (_eventHeight * (idx + 1)))
    }

    private fun getEventLineBottom(idx : Int) : Float {
        return (eventTop + (_eventBetweenPadding * idx) + (_eventLineHeight * (idx + 1)))
    }

    private fun drawPrevMonthRect(prev : Int, canvas : Canvas?) {
        if (prev <= 7) {
            canvas!!.drawRect(
                RectF(
                    0f,
                    0f,
                    (prev % DAYS_PER_WEEK).toFloat() * cellWidth,
                    cellHeight.toFloat()
                ),
                alphaPaint
            )
        }
        else {
            canvas!!.drawRect(
                RectF(
                    0f,
                    0f,
                    (7 * cellWidth).toFloat(),
                    cellHeight.toFloat()
                ),
                alphaPaint
            )
            canvas!!.drawRect(
                RectF(
                    0f,
                    cellHeight.toFloat(),
                    (prev % DAYS_PER_WEEK).toFloat() * cellWidth,
                    (2 * cellHeight).toFloat()
                ),
                alphaPaint
            )
        }
    }

    private fun drawNextMonthRect(next : Int, canvas : Canvas?) {
        if (next <= 7) {
            canvas!!.drawRect(
                RectF(
                    ((42 - next) % DAYS_PER_WEEK).toFloat() * cellWidth,
                    ((42 - next) / DAYS_PER_WEEK).toFloat() * cellHeight,
                    (7 * cellWidth).toFloat(),
                    (6 * cellHeight).toFloat(),
                ),
                alphaPaint
            )
        }
        else {
            canvas!!.drawRect(
                RectF(
                    ((42 - next) % DAYS_PER_WEEK).toFloat() * cellWidth,
                    ((42 - next) / DAYS_PER_WEEK ).toFloat() * cellHeight,
                    (7 * cellWidth).toFloat(),
                    (5 * cellHeight).toFloat(),
                ),
                alphaPaint
            )
            canvas!!.drawRect(
                RectF(
                    0f,
                    (5 * cellHeight).toFloat(),
                    (7 * cellWidth).toFloat(),
                    (6 * cellHeight).toFloat(),
                ),
                alphaPaint
            )
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

    private fun setLineRect(order : Int, startIdx : Int, endIdx : Int) : RectF {
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventLineHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventLineHeight * (order + 1))
        )
    }

    private fun setBgPaintColor(event: Event) {
        Log.d("BG_COLOR_CHECK", categoryList.toString())
        Log.d("BG_COLOR_CHECK", event.toString())
        Log.d("BG_COLOR_CHECK", "Category Server : " + event.categoryServerIdx + " | Category Idx : " + event.categoryIdx)

        val foundCategory = categoryList.find {
            if (it.serverIdx != 0L) it.serverIdx == event.categoryServerIdx
            else it.categoryIdx == event.categoryIdx
        }

        bgPaint.color = foundCategory?.color ?: R.color.schedule
    }

    private fun getEventTextStart(title : String, startIdx : Int, endIdx : Int) : Float {
        return (((startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding) + ((endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding)) / 2 - eventBounds.width() / 2
    }

    private fun getEventTextBottom(title : String, startIdx : Int, endIdx : Int, order : Int) : Float {
        return (((startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order) + ((endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1)))) / 2 + eventBounds.height() / 2
    }

    private fun isSameMonth(date : DateTime) : Boolean {
        if (date.monthOfYear != DateTime(millis).monthOfYear) {
            return false
        }
        return true
    }

    fun setDayList(millis : Long) {
        this.millis = millis
        dayList.clear()
        dayList.addAll(getMonthList(DateTime(millis)))
    }

    fun setEventList(events : List<Event>) {
        eventList.clear()
        eventList.addAll(events)

        invalidate()
    }

    fun setCategoryList(category : List<Category>) {
        categoryList.clear()
        categoryList.addAll(category)
    }

    fun getDayList() : MutableList<DateTime> {
        return dayList
    }

    fun getEventList() : MutableList<Event> {
        return eventList
    }
}