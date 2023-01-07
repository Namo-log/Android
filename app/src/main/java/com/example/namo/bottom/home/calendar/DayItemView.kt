package com.example.namo.bottom.home.calendar

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import android.graphics.drawable.shapes.RoundRectShape
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import com.example.namo.R
import com.example.namo.bottom.home.calendar.events.Event
import com.example.namo.utils.CalendarUtils.Companion.getDateColor
import com.example.namo.utils.CalendarUtils.Companion.isSameMonth
import org.joda.time.DateTime

class DayItemView @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    @AttrRes private val defStyleAttr : Int = R.attr.itemViewStyle,
    @StyleRes private val defStyleRes : Int = R.style.Calendar_ItemViewStyle,
    private val date : DateTime = DateTime(),
    private val firstDayOfMonth : DateTime = DateTime(),
    private val eventList : ArrayList<Event> = arrayListOf()
) : View(ContextThemeWrapper(context, defStyleRes), attrs, defStyleAttr) {

    private val bounds = Rect()
    private val eventBounds = Rect()
    private val moreBounds = Rect()
    private var showTitle: Boolean = false
    private var paint: Paint = Paint()
    private var bgPaint: Paint = Paint()
    private var eventPaint: Paint = Paint()
    private var morePaint : Paint = Paint()

    private var path = Path()
    private var rect = RectF()
    private lateinit var corners: FloatArray
    private var eventPos : Int = 0
    private var eventTop : Float = 0f
    private var _eventHeight: Float = 0f
    private var _eventTopPadding: Float = 0f
    private var _eventMorePadding: Float = 0f
    private var _eventBetweenPadding: Float = 0f
    private var _eventHorizontalPadding: Float = 0f
    private var _eventCornerRadius : Float = 0f

    private var moreText : String = ""
    //여기부터 수정

    private var today = DateTime().withTimeAtStartOfDay().millis

    init {
        context.withStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, defStyleRes) {
            val dayTextSize =
                getDimensionPixelSize(R.styleable.CalendarView_dayTextSize, 0).toFloat()
            val eventTextSize =
                getDimensionPixelSize(R.styleable.CalendarView_eventTextSize, 0).toFloat()
            _eventHeight = getDimension(R.styleable.CalendarView_eventHeight, 0f)
            _eventTopPadding = getDimension(R.styleable.CalendarView_eventTopPadding, 0f)
            _eventMorePadding = getDimension(R.styleable.CalendarView_eventMorePadding, 0f)
            _eventBetweenPadding = getDimension(R.styleable.CalendarView_eventBetweenPadding, 0f)
            _eventHorizontalPadding = getDimension(R.styleable.CalendarView_eventHorizontalPadding, 0f)
            _eventCornerRadius = getDimension(R.styleable.CalendarView_eventCornerRadius, 0f)

            paint = TextPaint().apply {
                isAntiAlias = true
                textSize = dayTextSize
                typeface = Typeface.DEFAULT_BOLD
                color = getDateColor(isToday(), context)
                if (!isSameMonth(date, firstDayOfMonth)) {
                    alpha = 50
                }
            }

            eventPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = eventTextSize
                color = Color.BLACK
                typeface = Typeface.DEFAULT
                if (!isSameMonth(date, firstDayOfMonth)) {
                    alpha = 50
                }
            }

            morePaint = TextPaint().apply {
                isAntiAlias = true
                textSize = eventTextSize
                color = Color.BLACK
                typeface = Typeface.DEFAULT
                if (!isSameMonth(date, firstDayOfMonth)) {
                    alpha = 50
                }
            }

            bgPaint.apply {
                color = resources.getColor(R.color.palette3)
                if (!isSameMonth(date, firstDayOfMonth)) {
                    alpha = 50
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        val day = date.dayOfMonth.toString()

        paint.getTextBounds(day, 0, day.length, bounds)
        canvas.drawText(
            day,
            (width / 2 - bounds.width() / 2).toFloat(),
            (bounds.height()).toFloat(),
            paint
        )

        eventTop = bounds.height() + _eventTopPadding

        for (i in 0 until eventList.size) {

            if (height - getEventBottom(i) < _eventHeight) {
                setMore(i)
                canvas.drawText(
                    moreText,
                    (width / 2 - moreBounds.width() / 2).toFloat(),
                    height - _eventMorePadding,
                    morePaint
                )
                break
            }

            corners = getRound(eventList[i])
            rect = setRect(eventList[i])

            val path = Path()
            path.addRoundRect(rect, corners, Path.Direction.CW)
            setBgPaintColor(eventList[i])
            canvas.drawPath(path, bgPaint)

            if (showTitle) {
                eventPaint.getTextBounds(eventList[i].title, 0, eventList[i].title.length, eventBounds)
                canvas.drawText(
                    eventList[i].title,
                    (width / 2 - eventBounds.width() / 2).toFloat(),
                    getTextBottom(eventList[i].idx),
                    eventPaint
                )
            }

        }

//        eventList.forEach {
//            corners = getRound(it)
//            rect = setRect(it)
//
//            val path = Path()
//            path.addRoundRect(rect, corners, Path.Direction.CW)
//            setBgPaintColor(it)
//            canvas.drawPath(path, bgPaint)
//
//            if (showTitle) {
//                eventPaint.getTextBounds(it.title, 0, it.title.length, eventBounds)
////                Log.d("PAINT_PAINT", paint.textSize.toString())
////                Log.d("PAINT_EVENT", eventPaint.textSize.toString())
//                canvas.drawText(
//                    it.title,
//                    (width / 2 - eventBounds.width() / 2).toFloat(),
//                    getTextBottom(it.idx),
//                    eventPaint
//                )
//            }
//        }

        setOnClickListener {
            Log.d(
                "DAYITEMVIEW_CLICK",
                "${date.year}년 ${date.monthOfYear}월 ${date.dayOfMonth}일 ${date.dayOfWeek}요일"
            )
        }
    }

    private fun setMore(idx : Int) {
        var num = eventList.size - idx
        moreText = "+$num"

        morePaint.getTextBounds(moreText, 0, moreText.length, moreBounds)
    }

    private fun getEventBottom(idx : Int) : Float {
        return (eventTop + (_eventBetweenPadding * idx) + (_eventHeight * (idx + 1)))
    }

    private fun setBgPaintColor(event: Event) {
        bgPaint.color = resources.getColor(event.color)
        if (!isSameMonth(date, firstDayOfMonth)) {
            bgPaint.alpha = 50
        }
    }

    private fun isToday() : Boolean {
        if (date.withTimeAtStartOfDay().isEqual(today)) {
            return true
        }
        return false
    }

    private fun getTextBottom(idx : Int) : Float {
        var top = (eventTop + ((_eventBetweenPadding + _eventHeight) * idx))
        var bottom = (eventTop + (_eventBetweenPadding * idx) + (_eventHeight * (idx + 1)))
        var middle = (top + bottom) / 2

        return (middle + eventBounds.height() / 2) - 2f
    }

    private fun setRect(it : Event) : RectF {
        when(eventPos) {
            0 -> { //one day
                rect = RectF(
                    _eventHorizontalPadding,
                    (eventTop + ((_eventBetweenPadding + _eventHeight) * it.idx)),
                    width.toFloat() - _eventHorizontalPadding,
                    (eventTop + (_eventBetweenPadding * it.idx) + (_eventHeight * (it.idx + 1)))
                )
            }
            1 -> { //start
                rect = RectF(
                    _eventHorizontalPadding,
                    (eventTop + ((_eventBetweenPadding + _eventHeight) * it.idx)),
                    width.toFloat(),
                    (eventTop + (_eventBetweenPadding * it.idx) + (_eventHeight * (it.idx + 1)))
                )
            }
            2 -> { //middle
                rect = RectF(
                    0f,
                    (eventTop + ((_eventBetweenPadding + _eventHeight) * it.idx)),
                    width.toFloat(),
                    (eventTop + (_eventBetweenPadding * it.idx) + (_eventHeight * (it.idx + 1)))
                )
            }
            3 -> { //end
                rect = RectF(
                    0f,
                    (eventTop + ((_eventBetweenPadding + _eventHeight) * it.idx)),
                    width.toFloat() - _eventHorizontalPadding,
                    (eventTop + (_eventBetweenPadding * it.idx) + (_eventHeight * (it.idx + 1)))
                )
            }
            else -> {
                rect = RectF(
                    0f,
                    (eventTop + ((_eventBetweenPadding + _eventHeight) * it.idx)),
                    width.toFloat(),
                    (eventTop + (_eventBetweenPadding * it.idx) + (_eventHeight * (it.idx + 1)))
                )
            }
        }
        return rect
    }

    //일요일이면 왼쪽, 토요일이면 오른쪽
    //시작이면 왼쪽, 끝이면 오른쪽, 일정이 하루면 양쪽
    private fun getRound(event: Event): FloatArray {
        if (date.withTimeAtStartOfDay().isEqual(DateTime(event.startLong).withTimeAtStartOfDay()) &&
            date.withTimeAtStartOfDay().isEqual(DateTime(event.endLong).withTimeAtStartOfDay())) {
            //일정이 하루
            val corners = floatArrayOf(
                _eventCornerRadius, _eventCornerRadius, //Top left
                _eventCornerRadius, _eventCornerRadius, //Top right
                _eventCornerRadius, _eventCornerRadius, //Bottom right
                _eventCornerRadius, _eventCornerRadius //Bottom left
            )
            eventPos = 0
            showTitle = true

            return corners
        }
        else if(date.dayOfWeek == 7) {
            //일요일
            showTitle = true

            if (date.withTimeAtStartOfDay().isEqual(DateTime(event.endLong).withTimeAtStartOfDay())) {
                //일요일인데 종료일 -> 모서리 다 둥글게, 양쪽 패딩 -> 일정이 하루일 때외 같음
                val corners = floatArrayOf(
                    _eventCornerRadius, _eventCornerRadius, //Top left
                    _eventCornerRadius, _eventCornerRadius, //Top right
                    _eventCornerRadius, _eventCornerRadius, //Bottom right
                    _eventCornerRadius, _eventCornerRadius //Bottom left
                )
                eventPos = 0

                return corners
            }
            else {
                //일요일인데 종료일은 아님 -> 왼쪽만 둥글게, start와 같음
                val corners = floatArrayOf(
                    _eventCornerRadius, _eventCornerRadius, //Top left
                    0f, 0f, //Top right
                    0f, 0f, //Bottom right
                    _eventCornerRadius, _eventCornerRadius //Bottom left
                )
                eventPos = 1

                return corners
            }
        }
        else if(date.dayOfWeek == 6) {
            //토요일
            if (date.withTimeAtStartOfDay().isEqual(DateTime(event.startLong).withTimeAtStartOfDay())) {
                //토요일인데 시작일 -> 모서리 다 둥글게, 양쪽 패딩 -> 일정이 하루일 때외 같음
                val corners = floatArrayOf(
                    _eventCornerRadius, _eventCornerRadius, //Top left
                    _eventCornerRadius, _eventCornerRadius, //Top right
                    _eventCornerRadius, _eventCornerRadius, //Bottom right
                    _eventCornerRadius, _eventCornerRadius //Bottom left
                )
                eventPos = 0
                showTitle = true

                return corners
            }
            else {
                //토요일인데 시작일은 아님 -> 오른쪽만 둥글게, end와 같음
                val corners = floatArrayOf(
                    0f, 0f, //Top left
                    _eventCornerRadius, _eventCornerRadius, //Top right
                    _eventCornerRadius, _eventCornerRadius, //Bottom right
                    0f, 0f //Bottom left
                )
                eventPos = 3
                showTitle = false

                return corners
            }
        }


        if (date.withTimeAtStartOfDay().isEqual(DateTime(event.startLong).withTimeAtStartOfDay())) {
            //start
            val corners = floatArrayOf(
                _eventCornerRadius, _eventCornerRadius, //Top left
                0f, 0f, //Top right
                0f, 0f, //Bottom right
                _eventCornerRadius, _eventCornerRadius //Bottom left
            )
            eventPos = 1
            showTitle = true

            return corners
        } else if (date.withTimeAtStartOfDay().isEqual(DateTime(event.endLong).withTimeAtStartOfDay())) {
            //end
            val corners = floatArrayOf(
                0f, 0f, //Top left
                _eventCornerRadius, _eventCornerRadius, //Top right
                _eventCornerRadius, _eventCornerRadius, //Bottom right
                0f, 0f //Bottom left
            )
            eventPos = 3
            showTitle = false

            return corners
        }
        else {
            val corners = floatArrayOf(
                0f, 0f, //Top left
                0f, 0f, //Top right
                0f, 0f, //Bottom right
                0f, 0f //Bottom left
            )
            eventPos = 2
            showTitle = false

            return corners
        }
    }
}