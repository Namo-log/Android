package com.example.namo.ui.bottom.home.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.view.children
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.example.namo.utils.CalendarUtils.Companion.getOrder
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.DAYS_PER_WEEK


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

    var detector: GestureDetector? = null

    lateinit var db : NamoDatabase

    private lateinit var firstDayOfMonth : DateTime
    private var list : List<DateTime> = listOf()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec), getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        Log.d("SIZE", "Oldh : ${oldh}, h : $h")
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility", "DrawAllocation")
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

//        //터치를 했을때 작동하는 메서드
//        //터치를 했을때 작동하는 메서드
//        setOnTouchListener(OnTouchListener { v, event ->
//            val action = event.action
//            val curX = event.x //눌린 곳의 X좌표
//            val curY = event.y //눌린 곳의 Y좌표
//            if (action == MotionEvent.ACTION_DOWN) {   //처음 눌렸을 때
//                Log.d("TOUCH_COOR", "손가락 눌림 : $curX, $curY")
//            } else if (action == MotionEvent.ACTION_MOVE) {    //누르고 움직였을 때
//                Log.d("TOUCH_COOR", "손가락 움직임 : $curX, $curY")
//            } else if (action == MotionEvent.ACTION_UP) {    //누른걸 뗐을 때
//                Log.d("TOUCH_COOR", "손가락 뗌 : $curX, $curY")
//            }
//            true
//        })
//
//        detector = GestureDetector(context, object : GestureDetector.OnGestureListener {
//            //화면이 눌렸을 때
//            override fun onDown(e: MotionEvent): Boolean {
//                Log.d("TOUCH_COOR", "onDown() 호출됨")
//                return true
//            }
//
//            //화면이 눌렸다 떼어지는 경우
//            override fun onShowPress(e: MotionEvent) {
//                Log.d("TOUCH_COOR", "onShowPress() 호출됨")
//            }
//
//            //화면이 한 손가락으로 눌렸다 떼어지는 경우
//            override fun onSingleTapUp(e: MotionEvent): Boolean {
//                Log.d("TOUCH_COOR", "onSingleTapUp() 호출됨")
//                return true
//            }
//
//            //화면이 눌린채 일정한 속도와 방향으로 움직였다 떼어지는 경우
//            override fun onScroll(
//                e1: MotionEvent,
//                e2: MotionEvent,
//                distanceX: Float,
//                distanceY: Float
//            ): Boolean {
//                Log.d("TOUCH_COOR", "onScroll() 호출됨 => $distanceX, $distanceY")
//                return false
//            }
//
//            //화면을 손가락으로 오랫동안 눌렀을 경우
//            override fun onLongPress(e: MotionEvent) {
//                Log.d("TOUCH_COOR", "onLongPress() 호출됨")
//            }
//
//            //화면이 눌린채 손가락이 가속해서 움직였다 떼어지는 경우
//            override fun onFling(
//                e1: MotionEvent,
//                e2: MotionEvent,
//                velocityX: Float,
//                velocityY: Float
//            ): Boolean {
//                Log.d("TOUCH_COOR", "")
//                return true
//            }
//        })
//
//        setOnTouchListener(OnTouchListener { v, event ->
//            detector!!.onTouchEvent(event)
//            true
//        })

    }


    /**
     * 달력 그리기 시작한다.
     * @param firstDayOfMonth   한 달의 시작일
     * @param list              달력이 가지고 있는 요일과 이벤트 목록 (총 42개)
     */
    fun initCalendar(firstDayOfMonth : DateTime, list : List<DateTime>) {
        removeAllViewsInLayout()
        this.firstDayOfMonth = firstDayOfMonth
        this.list = list
        Log.d("CALENDAR_VIEW","init_calendar")
        //eventList : 이 달력의 일정
        db = NamoDatabase.getInstance(context)
        var eventList : ArrayList<Event> = arrayListOf()
        var forCalendarEvent : Thread = Thread {
            eventList = db.eventDao.getEventCalendar(
                list[0].withTimeAtStartOfDay().millis,
                list[41].plusDays(1).withTimeAtStartOfDay().millis - 1
            ) as ArrayList<Event>
        }
        forCalendarEvent.start()
        try {
            forCalendarEvent.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }

        list.forEach {
            var events = ArrayList<Event>() //오늘의 일정
            var forTodayEvent : Thread = Thread {
                events = db.eventDao.getEventDaily(
                    it.withTimeAtStartOfDay().millis,
                    it.plusDays(1).withTimeAtStartOfDay().millis - 1
                ) as ArrayList<Event>
            }
            forTodayEvent.start()
            try {
                forTodayEvent.join()
            } catch (e : InterruptedException) {
                e.printStackTrace()
            }

            events.forEach {
                it.order = getOrder(it, eventList)
            }

            val view = DayItemView(
                context = context,
                date = it,
                firstDayOfMonth = firstDayOfMonth,
                eventList = events
            )

            addView(view)
        }
    }
}