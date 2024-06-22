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
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isScroll = false

    var cellWidth = 0f
    var cellHeight = 0f
    private val bounds = Rect()
    private val today = DateTime.now().withTimeAtStartOfDay().millis

    val days = mutableListOf<DateTime>()
    val categoryList = mutableListOf<Category>()
    private val orderList = mutableListOf<Int>()
    val moreList = mutableListOf<Int>()
    private val otherRect: ArrayList<RectF> = arrayListOf()

    private val bounds2 = Rect()
    val eventBounds = Rect()
    val moreBounds = Rect()
    var showTitle: Boolean = false
    private var cellPaint: Paint = Paint()
    private var alphaPaint: Paint = Paint()
    private var datePaint: Paint = Paint()
    private var todayPaint: Paint = Paint()
    private var selectedPaint: Paint = Paint()
    private var clickPaint: Paint = Paint()
    var bgPaint: Paint = Paint()
    var eventPaint: TextPaint = TextPaint()
    var morePaint: Paint = Paint()

    private var todayNoticePaint: Paint = Paint()
    private var radius: Float = 34f

    private var path = Path()
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

            initPaints(dayTextSize, eventTextSize)
            corners = floatArrayOf(
                _eventCornerRadius, _eventCornerRadius, //Top left
                _eventCornerRadius, _eventCornerRadius, //Top right
                _eventCornerRadius, _eventCornerRadius, //Bottom right
                _eventCornerRadius, _eventCornerRadius //Bottom left
            )
        }
    }

    private fun initPaints(dayTextSize: Float, eventTextSize: Float) {
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
        }

        eventPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = eventTextSize
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
        }

        morePaint = TextPaint().apply {
            isAntiAlias = true
            textSize = eventTextSize
            color = Color.BLACK
            typeface = Typeface.DEFAULT_BOLD
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
                    handleDateClick(event)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleDateClick(event: MotionEvent) {
        val row = (event.y / cellHeight).toInt()
        val col = (event.x / cellWidth).toInt()
        if (row in 0..5 && col in 0..6) {
            val day = days[row * 7 + col]
            onDateClickListener?.onDateClick(day, row * 7 + col)
        }
    }

    fun findMaxOrderInSchedule(startIdx: Int, endIdx: Int): Int {
        return orderList.subList(startIdx, endIdx + 1).maxOrNull() ?: 0
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

    private fun drawPrevMonthRect(prev: Int, canvas: Canvas?) {
        val topHeight = if (prev <= 7) cellHeight else 2 * cellHeight
        canvas!!.drawRect(
            RectF(
                0f,
                0f,
                (prev % DAYS_PER_WEEK).toFloat() * cellWidth,
                topHeight
            ),
            alphaPaint
        )
    }

    private fun drawNextMonthRect(next: Int, canvas: Canvas?) {
        val bottomHeight = if (next <= 7) 6 * cellHeight else 5 * cellHeight
        canvas!!.drawRect(
            RectF(
                ((42 - next) % DAYS_PER_WEEK).toFloat() * cellWidth,
                ((42 - next) / DAYS_PER_WEEK).toFloat() * cellHeight,
                (7 * cellWidth),
                bottomHeight,
            ),
            alphaPaint
        )
    }

    fun splitWeek(startIdx: Int, endIdx: Int): ArrayList<StartEnd> {
        val result = ArrayList<StartEnd>()
        var start = if (startIdx == -1) 0 else startIdx
        val end = if (endIdx == -1) 41 else endIdx

        while (start <= end) {
            val mid = (start / 7) * 7 + 6
            result.add(StartEnd(start, mid.coerceAtMost(end)))
            start = mid + 1
        }
        return result
    }

    fun setRect(order: Int, startIdx: Int, endIdx: Int): RectF {
        val additionalMargin = dpToPx(context, 5f)
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding + additionalMargin,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1))
        )
    }

    fun setLineRect(order: Int, startIdx: Int, endIdx: Int): RectF {
        val additionalMargin = dpToPx(context, 5f)
        return RectF(
            (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding + additionalMargin,
            (startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventLineHeight) * order,
            (endIdx % DAYS_PER_WEEK) * cellWidth + cellWidth - _eventHorizontalPadding,
            (endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventLineHeight * (order + 1))
        )
    }

    fun getScheduleTextStart(startIdx: Int): Float {
        val startX = (startIdx % DAYS_PER_WEEK) * cellWidth + _eventHorizontalPadding
        val additionalMargin = dpToPx(context, 7f)
        return startX + additionalMargin
    }

    fun getScheduleTextBottom(title: String, startIdx: Int, endIdx: Int, order: Int): Float {
        return (((startIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding + _eventHeight) * order) + ((endIdx / DAYS_PER_WEEK) * cellHeight + eventTop + (_eventBetweenPadding * order) + (_eventHeight * (order + 1)))) / 2 + eventBounds.height() / 2
    }

    private fun isSameMonth(date: DateTime): Boolean {
        return date.monthOfYear == DateTime(millis).monthOfYear
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
        val padding = dpToPx(context, 5f)  // 5dp를 픽셀로 변환

        for (day in 0 until 42) {
            val x = (day % DAYS_PER_WEEK) * cellWidth + padding  // X 좌표를 오른쪽으로 5dp 이동
            val y = (day / DAYS_PER_WEEK) * cellHeight

            val dayString = days[day].dayOfMonth.toString()
            if (days[day].isEqual(today)) {
                todayPaint.getTextBounds(dayString, 0, dayString.length, bounds)
                val textWidth = todayPaint.measureText(dayString)
                val textHeight = bounds.height()

                canvas.drawCircle(
                    x + textWidth / 2,  // 원의 중심 X 좌표를 텍스트 중앙으로 조정
                    y + _dayTextHeight - textHeight / 2,  // 원의 중심 Y 좌표를 텍스트 중앙으로 조정
                    bounds.height().toFloat(),
                    todayNoticePaint
                )
                canvas.drawText(
                    dayString,
                    x,
                    y + _dayTextHeight,
                    todayPaint
                )
            } else {
                datePaint.getTextBounds(dayString, 0, dayString.length, bounds)
                canvas.drawText(
                    dayString,
                    x,
                    y + _dayTextHeight,
                    datePaint
                )
            }
        }
    }

    private fun drawSelected(canvas: Canvas) {
        val padding = dpToPx(context, 5f)  // 5dp를 픽셀로 변환

        if (selectedDate != null) {
            val selectedDay = selectedDate!!.dayOfMonth

            for (i in days.indices) {
                if (days[i] == selectedDate && !days[i].isEqual(today)) {
                    val x = (i % DAYS_PER_WEEK) * cellWidth + padding  // X 좌표를 오른쪽으로 5dp 이동
                    val y = (i / DAYS_PER_WEEK) * cellHeight

                    selectedPaint.getTextBounds(
                        selectedDay.toString(),
                        0,
                        selectedDay.toString().length,
                        bounds
                    )
                    canvas!!.drawText(
                        selectedDay.toString(),
                        x,
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
