package com.mongmong.namo.presentation.utils

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.presentation.ui.home.calendar.data.StartEnd
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.DAYS_PER_WEEK
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.dpToPx
import com.mongmong.namo.presentation.utils.CalendarUtils.Companion.getMonthList
import org.joda.time.DateTime
import kotlin.math.abs

abstract class CustomCalendarView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    interface OnDateClickListener {
        fun onDateClick(date: DateTime?, pos: Int?)
    }

    var onDateClickListener: OnDateClickListener? = null
    var selectedDate: DateTime? = null
    var millis: Long = 0
    var startX = 0f
    var startY = 0f
    var endX = 0f
    var endY = 0f
    var isScroll = false

    var cellWidth = 0f
    var cellHeight = 0f
    val bounds = Rect()
    val today = DateTime.now().withTimeAtStartOfDay().millis

    val days = mutableListOf<DateTime>()
    val categoryList = mutableListOf<Category>()
    val orderList = mutableListOf<Int>()
    val moreList = mutableListOf<Int>()
    val otherRect: ArrayList<RectF> = arrayListOf()

    val bounds2 = Rect()
    val eventBounds = Rect()
    val moreBounds = Rect()
    var showTitle: Boolean = false
    var cellPaint: Paint = Paint()
    var alphaPaint: Paint = Paint()
    var datePaint: Paint = Paint()
    var todayPaint: Paint = Paint()
    var selectedPaint: Paint = Paint()
    var clickPaint: Paint = Paint()
    var bgPaint: Paint = Paint()
    var eventPaint: TextPaint = TextPaint()
    var morePaint: Paint = Paint()

    var todayNoticePaint: Paint = Paint()
    var radius: Float = 34f

    var path = Path()
    var rect = RectF()
    var corners: FloatArray = floatArrayOf()
    var eventPos: Int = 0
    var eventTop: Float = 0f
    var _dayTextHeight: Float = 0f
    var _eventHeight: Float = 0f
    var _eventTopPadding: Float = 0f
    var _eventMorePadding: Float = 0f
    var _eventBetweenPadding: Float = 0f
    var _eventHorizontalPadding: Float = 0f
    var _eventCornerRadius: Float = 0f
    var _eventLineHeight: Float = 0f

    init {
        context.withStyledAttributes(
            attrs,
            R.styleable.CalendarView,
            defStyleAttr = R.attr.itemViewStyle,
            defStyleRes = R.style.Calendar_ItemViewStyle
        ) {
            val dayTextSize =
                getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()
            val eventTextSize =
                getDimensionPixelSize(R.styleable.CalendarView_eventTextSize, 0).toFloat()
            _dayTextHeight = getDimension(R.styleable.CalendarView_dayTextHeight, 0f)
            _eventHeight = getDimension(R.styleable.CalendarView_eventHeight, 0f)
            _eventTopPadding = getDimension(R.styleable.CalendarView_eventTopPadding, 0f)
            _eventMorePadding = getDimension(R.styleable.CalendarView_eventMorePadding, 0f)
            _eventBetweenPadding = getDimension(R.styleable.CalendarView_eventBetweenPadding, 0f)
            _eventHorizontalPadding =
                getDimension(R.styleable.CalendarView_eventHorizontalPadding, 0f)
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
                color = context.getColor(R.color.black)
            }

            todayPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = context.getColor(R.color.white)
            }

            todayNoticePaint.color = context.getColor(R.color.MainOrange)

            selectedPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = context.getColor(R.color.MainOrange)
            }

            bgPaint.apply {
                color = context.getColor(R.color.palette3)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
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
                        val day = days[row * 7 + col]
                        onDateClickListener?.onDateClick(day, row * 7 + col)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun findMaxOrderInSchedule(startIdx: Int, endIdx: Int): Int {
        var maxOrder = 0
        for (i in startIdx..endIdx) {
            if (orderList[i] > maxOrder) maxOrder = orderList[i]
        }

        return maxOrder
    }

    fun setOrder(order: Int, startIdx: Int, endIdx: Int) {
        for (i in startIdx..endIdx) {
            orderList[i] = order + 1
        }
    }


    fun getScheduleBottom(idx: Int): Float {
        return (eventTop + (_eventBetweenPadding * idx) + (_eventHeight * (idx + 1)))
    }

    fun getScheduleLineBottom(idx: Int): Float {
        return (eventTop + (_eventBetweenPadding * idx) + (_eventLineHeight * (idx + 1)))
    }

    fun drawPrevMonthRect(prev: Int, canvas: Canvas?) {
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
        } else {
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

    fun drawNextMonthRect(next: Int, canvas: Canvas?) {
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
        } else {
            canvas!!.drawRect(
                RectF(
                    ((42 - next) % DAYS_PER_WEEK).toFloat() * cellWidth,
                    ((42 - next) / DAYS_PER_WEEK).toFloat() * cellHeight,
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

    fun setRect(order: Int, startIdx: Int, endIdx: Int): RectF {
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1))
        )
    }
    fun splitWeek(startIdx: Int, endIdx: Int) : ArrayList<StartEnd> {
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
    fun setLineRect(order: Int, startIdx: Int, endIdx: Int): RectF {
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventLineHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventLineHeight * (order + 1))
        )
    }

    fun getScheduleTextStart(startIdx: Int): Float {
        val startX = (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding
        val additionalMargin = dpToPx(context, 2f)  // 여기에서 context는 해당 뷰 또는 액티비티의 컨텍스트입니다.
        return startX + additionalMargin
    }


    fun getScheduleTextBottom(title: String, startIdx: Int, endIdx: Int, order: Int): Float {
        return (((startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order) + ((endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1)))) / 2 + eventBounds.height() / 2
    }

    fun isSameMonth(date: DateTime): Boolean {
        if (date.monthOfYear != DateTime(millis).monthOfYear) {
            return false
        }
        return true
    }

    fun setDays(millis: Long) {
        this.millis = millis
        days.clear()
        days.addAll(getMonthList(DateTime(millis)))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDays(canvas)
        drawSelected(canvas)
        drawSchedules(canvas)
        drawRestDays(canvas)
    }

    private fun drawDays(canvas: Canvas) {
        for (day in 0 until 42) {
            val x = (day % DAYS_PER_WEEK) * cellWidth
            val y = (day / DAYS_PER_WEEK) * cellHeight

            if (days[day].isEqual(today)) {
                todayPaint.getTextBounds(
                    days[day].dayOfMonth.toString(),
                    0,
                    days[day].dayOfMonth.toString().length,
                    bounds
                )
                canvas!!.drawCircle(
                    (x + cellWidth / 2),
                    (y + _dayTextHeight - bounds.height() / 2),
                    bounds.height().toFloat(),
                    todayNoticePaint
                )
                canvas!!.drawText(
                    days[day].dayOfMonth.toString(),
                    (x + cellWidth / 2 - bounds.right.toFloat() / 2),
                    y + _dayTextHeight,
                    todayPaint
                )
            } else {
                datePaint.getTextBounds(
                    days[day].dayOfMonth.toString(),
                    0,
                    days[day].dayOfMonth.toString().length,
                    bounds
                )
                canvas!!.drawText(
                    days[day].dayOfMonth.toString(),
                    (x + cellWidth / 2 - bounds.right.toFloat() / 2),
                    y + _dayTextHeight,
                    datePaint
                )
            }

        }
    }

    private fun drawSelected(canvas: Canvas) {
        if (selectedDate != null) {
            val selectedDay = selectedDate!!.dayOfMonth
            for (i in days.indices) {
                if (days[i] == selectedDate && !days[i].isEqual(today)) {
                    val x = (i % DAYS_PER_WEEK) * cellWidth
                    val y = (i / DAYS_PER_WEEK) * cellHeight

                    selectedPaint.getTextBounds(
                        selectedDay.toString(),
                        0,
                        selectedDay.toString().length,
                        bounds
                    )
                    canvas!!.drawText(
                        selectedDay.toString(),
                        (x + cellWidth / 2 - bounds.right.toFloat() / 2),
                        y + _dayTextHeight,
                        selectedPaint
                    )
                    break
                }
            }
        }

        eventTop = _dayTextHeight + _eventTopPadding

        orderList.clear()
        for (i in 0 until 42) orderList.add(0)
        moreList.clear()
        for (i in 0 until 42) moreList.add(0)
    }
    private fun drawRestDays(canvas: Canvas) {
        // 이전달, 다음달은 불투명하게
        var prev = 0
        var next = 0
        while (!isSameMonth(days[prev])) prev++
        while (!isSameMonth(days[41 - next])) next++
        drawPrevMonthRect(prev, canvas)
        drawNextMonthRect(next, canvas)
    }
    abstract fun drawSchedules(canvas: Canvas)
}
