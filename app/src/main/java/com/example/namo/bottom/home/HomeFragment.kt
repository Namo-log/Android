package com.example.namo.bottom.home

import android.annotation.SuppressLint
import android.app.ActionBar.LayoutParams
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.bottom.home.calendar.CalendarAdapter
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.bottom.home.calendar.SetMonthDialog
import com.example.namo.bottom.home.calendar.events.Event
import com.example.namo.databinding.FragmentHomeBinding
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import com.example.namo.utils.CalendarUtils.Companion.getPrevOffset
import org.joda.time.DateTimeConstants.DAYS_PER_WEEK
import org.w3c.dom.Text
import kotlin.math.abs


class HomeFragment : Fragment() {

    private lateinit var calendarAdapter : CalendarAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var monthList : List<DateTime>

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2
    private var isScroll = false
    private var isShow = false
    private var prevIdx = -1
    private var nowIdx = 0
    private var totalWidth : Int = 0
    private var totalHeight : Int = 0
    private var dayWidth : Float = 0f
    private var dayHeight : Float = 0f

    private var startX : Float = 0f
    private var startY : Float = 0f
    private var endX : Float = 0f
    private var endY : Float = 0f

    private var event_personal : ArrayList<Event> = arrayListOf()
    private var event_group : ArrayList<Event> = arrayListOf()

    private val personalEventRVAdapter = DailyPersonalRVAdapter()
    private val groupEventRVAdapter = DailyGroupRVAdapter()

//    private val headerText : TextView by lazy {
//        requireActivity().findViewById(R.id.home_daily_header_tv)
//    }
//    private val scrollView : ScrollView by lazy {
//        requireActivity().findViewById(R.id.daily_scroll_sv)
//    }
//    private val rv : RecyclerView by lazy {
//        requireActivity().findViewById(R.id.home_daily_event_rv)
//    }
//    private val groupRv : RecyclerView by lazy {
//        requireActivity().findViewById(R.id.home_daily_group_event_rv)
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container,false)
        calendarAdapter = CalendarAdapter(context as MainActivity)

        //hideBottomNavigation(false)

        binding.homeCalendarTodayTv.text = DateTime().dayOfMonth.toString()

        binding.homeCalendarVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.homeCalendarVp.adapter = calendarAdapter
        binding.homeCalendarVp.setCurrentItem(CalendarAdapter.START_POSITION, false)
        setMillisText()

        binding.homeCalendarVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                Log.d("VIEWPAGER_PAGE_SELECTED", "position : $position")
                pos = position
                prevIdx = -1
                millis = binding.homeCalendarVp.adapter!!.getItemId(position)
                setMillisText()
                super.onPageSelected(position)
            }
        })

        setAdapter()
        clickListener()

        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListener() {

        var detector = GestureDetector(context, object : GestureDetector.OnGestureListener {
            override fun onDown(e: MotionEvent?): Boolean {
                Log.d("VP_TOUCH", "onDown")
                return false
            }

            override fun onShowPress(e: MotionEvent?) {
                Log.d("VP_TOUCH", "ShowPress")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                Log.d("VP_TOUCH", "onSingleTapUp")
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Log.d("VP_TOUCH", "onScroll() 호출됨 => $distanceX, $distanceY")
                return false
            }

            override fun onLongPress(e: MotionEvent?) {
                Log.d("VP_TOUCH", "onLongPress")
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("VP_TOUCH", "onFling")
                return false
            }

        })

        binding.constraintLayout4.setOnClickListener {
            Log.d("TOUCH","constraint layout")
        }

        binding.constraintLayout4.setOnTouchListener { v, event ->
            detector.onTouchEvent(event)
            false
        }

        binding.homeCalendarVp.getChildAt(0).setOnTouchListener { v, event ->
            detector.onTouchEvent(event)
            false
        }

        binding.homeCalendarVp.getChildAt(0).setOnTouchListener { v, event ->

            totalWidth = binding.homeCalendarVp.width
            totalHeight = binding.homeCalendarVp.height
            dayWidth = (totalWidth / DAYS_PER_WEEK).toFloat()
            dayHeight = (totalHeight / WEEKS_PER_MONTH).toFloat()

            val action = event.action
            val curX = event.x //눌린 곳의 X좌표
            val curY = event.y //눌린 곳의 Y좌표
            if (action == MotionEvent.ACTION_DOWN) {   //처음 눌렸을 때
//                Log.d("TOUCH_COOR", "손가락 눌림 : $curX, $curY")
                startX = curX
                startY = curY
            } else if (action == MotionEvent.ACTION_MOVE) {    //누르고 움직였을 때
//                Log.d("TOUCH_COOR", "손가락 움직임 : $curX, $curY")
            } else if (action == MotionEvent.ACTION_UP) {    //누른걸 뗐을 때
//                Log.d("TOUCH_COOR", "손가락 뗌 : $curX, $curY")
                endX = curX
                endY = curY
            }

            isScroll = !(abs(endX - startX) < 10 && abs(endY - startY) < 10)

            if (!isScroll) {
                nowIdx = (curY / dayHeight).toInt() * DAYS_PER_WEEK + (curX / dayWidth).toInt()
//                Log.d("TOUCH_IDX", "prev : $prevIdx   now : $nowIdx")
//                Log.d("TOUCH_DATE", monthList[nowIdx].toString("yyyy년 MM월 dd일"))
                setDaily(nowIdx)

//                Log.d("TOUCH_SHOW_PREV", "isShow : ${isShow}")
//                if (isShow && prevIdx == nowIdx) {
//                    binding.dailyLayout.visibility = View.GONE
//                    isShow = !isShow
//                }
//                else if (!isShow) {
//                    binding.dailyLayout.visibility = View.VISIBLE
//                    isShow = !isShow
//                }
//
//                Log.d("TOUCH_SHOW_AFTER", "isShow : ${isShow}")
                prevIdx = nowIdx
            }

            false
        }

        binding.homeCalendarYearMonthTv.setOnClickListener {
            SetMonthDialog(requireContext(), millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.homeCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }

        binding.homeCalendarTodayTv.setOnClickListener {
            binding.homeCalendarVp.setCurrentItem(todayPos, true)
            setToday()
        }

    }

    private fun setAdapter() {
        binding.bottomLayout.homeDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.bottomLayout.homeDailyEventRv.adapter = personalEventRVAdapter

        binding.bottomLayout.homeDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.bottomLayout.homeDailyGroupEventRv.adapter = groupEventRVAdapter
        setToday()
    }

    private fun setToday() {
        monthList = getMonthList(DateTime(System.currentTimeMillis()))
        prevIdx = monthList.indexOf(DateTime(System.currentTimeMillis()).withTimeAtStartOfDay())
        nowIdx = prevIdx
        setDaily(nowIdx)
    }

    private fun setDaily(idx : Int) {
        binding.bottomLayout.homeDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.bottomLayout.dailyScrollSv.scrollTo(0,0)
        setData(idx)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(idx : Int) {

        getDummy(idx)

        personalEventRVAdapter.addPersonal(event_personal)
        groupEventRVAdapter.addGroup(event_group)
        setEmptyMsg()

        requireActivity().runOnUiThread {
            personalEventRVAdapter.notifyDataSetChanged()
            groupEventRVAdapter.notifyDataSetChanged()
        }
    }

    private fun setEmptyMsg() {
        if (event_personal.size == 0 ) binding.bottomLayout.homeDailyEventNoneTv.visibility = View.VISIBLE
        else binding.bottomLayout.homeDailyEventNoneTv.visibility = View.GONE

        if (event_group.size == 0) binding.bottomLayout.homeDailyGroupEventNoneTv.visibility = View.VISIBLE
        else binding.bottomLayout.homeDailyGroupEventNoneTv.visibility = View.GONE
    }

    private fun getDummy(idx : Int) {
        event_personal.clear()
        event_group.clear()
        val date = monthList[idx]

        event_personal.apply {
            add(
                Event(
                    "개인1",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(1,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
                    R.color.palette1,
                    1
                )
            )
            add(
                Event(
                    "개인2",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(2,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,idx,0,0).millis),
                    R.color.palette2,
                    2
                )
            )
            add(
                Event(
                    "개인3",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(3,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,idx,0,0).millis),
                    R.color.palette3,
                    3
                )
            )
        }

        event_group.apply {
            add(
                Event(
                    "그룹4",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(1,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
                    R.color.palette4,
                    1
                )
            )
            add(
                Event(
                    "그룹5",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(2,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
                    R.color.palette5,
                    2
                )
            )
            add(
                Event(
                    "그룹6",
                    date.withTimeAtStartOfDay().millis,
                    date.withTime(3,date.dayOfMonth,0,0).millis,
                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
                    R.color.palette6,
                    3
                )
            )
        }
    }

    private fun setMillisText() {
        binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
        monthList = getMonthList(DateTime(millis))
        prevIdx = getPrevOffset(DateTime(millis).withDayOfMonth(1))
        nowIdx = prevIdx
        setDaily(nowIdx)
//        Log.d("PREV_IDX", prevIdx.toString())
//        Log.d("MONTH_LIST", monthList.toString())
    }

//    private fun hideBottomNavigation( bool : Boolean){
//        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
//        if(bool == true) {
//            bottomNavigationView.visibility = View.GONE
//        } else {
//            bottomNavigationView.visibility = View.VISIBLE
//        }
//    }
}