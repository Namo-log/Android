package com.example.namo.ui.bottom.home

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.ui.bottom.home.calendar.CalendarAdapter
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.ui.bottom.home.calendar.SetMonthDialog
import com.example.namo.data.entity.home.calendar.Event
import com.example.namo.ui.bottom.home.schedule.ScheduleDialogFragment
import com.example.namo.databinding.FragmentHomeBinding
import com.example.namo.ui.bottom.diary.DiaryDetailFragment
import com.example.namo.utils.CalendarUtils.Companion.WEEKS_PER_MONTH
import com.example.namo.utils.CalendarUtils.Companion.getInterval
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.joda.time.DateTime
import com.example.namo.utils.CalendarUtils.Companion.getMonthList
import com.example.namo.utils.CalendarUtils.Companion.getPrevOffset
import org.joda.time.DateTimeConstants.DAYS_PER_WEEK
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

    private var canvas : Canvas = Canvas()
    private var paint : Paint = Paint()

    private lateinit var scheduleDialogFragment : ScheduleDialogFragment

    lateinit var db : NamoDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container,false)
        db = NamoDatabase.getInstance(requireContext())
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

//        setAdapter()
        clickListener()
        Log.e("HOME_LIFECYCLE", "OnCreateView")

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Log.e("HOME_LIFECYCLE", "OnResume")
        setAdapter()
    }

    override fun onStop() {
        super.onStop()
        Log.e("HOME_LIFECYCLE", "OnStop")
    }

    override fun onPause() {
        super.onPause()
        Log.e("HOME_LIFECYCLE", "OnPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("HOME_LIFECYCLE", "OnDestory")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun clickListener() {

        binding.homeFab.setOnClickListener {
            scheduleDialogFragment = ScheduleDialogFragment {

            }
            scheduleDialogFragment.setDate(monthList[nowIdx])
            scheduleDialogFragment.show(requireActivity().supportFragmentManager, ScheduleDialogFragment.TAG)
        }

//        var detector = GestureDetector(context, object : GestureDetector.OnGestureListener {
//            override fun onDown(e: MotionEvent?): Boolean {
//                Log.d("VP_TOUCH", "onDown")
//                return false
//            }
//
//            override fun onShowPress(e: MotionEvent?) {
//                Log.d("VP_TOUCH", "ShowPress")
//            }
//
//            override fun onSingleTapUp(e: MotionEvent?): Boolean {
//                Log.d("VP_TOUCH", "onSingleTapUp")
//                return false
//            }
//
//            override fun onScroll(
//                e1: MotionEvent?,
//                e2: MotionEvent?,
//                distanceX: Float,
//                distanceY: Float
//            ): Boolean {
//                Log.d("VP_TOUCH", "onScroll() 호출됨 => $distanceX, $distanceY")
//                return false
//            }
//
//            override fun onLongPress(e: MotionEvent?) {
//                Log.d("VP_TOUCH", "onLongPress")
//            }
//
//            override fun onFling(
//                e1: MotionEvent?,
//                e2: MotionEvent?,
//                velocityX: Float,
//                velocityY: Float
//            ): Boolean {
//                Log.d("VP_TOUCH", "onFling")
//                return false
//            }
//
//        })

//        binding.constraintLayout4.setOnClickListener {
//            Log.d("TOUCH","constraint layout")
//        }

//        binding.constraintLayout4.setOnTouchListener { v, event ->
//            detector.onTouchEvent(event)
//            false
//        }

//        binding.homeCalendarVp.getChildAt(0).setOnTouchListener { v, event ->
//            detector.onTouchEvent(event)
//            false
//        }

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
//                setStroke(nowIdx)

                Log.d("TOUCH_SHOW_PREV", "isShow : ${isShow}")
                if (isShow && prevIdx == nowIdx) {
//                    binding.dailyLayout.visibility = View.GONE
//                    hideBottomNavigation(false)
                    binding.constraintLayout4.transitionToStart()
                    isShow = !isShow
                }
                else if (!isShow) {
//                    binding.dailyLayout.visibility = View.VISIBLE
//                    hideBottomNavigation(true)
                    binding.constraintLayout4.transitionToEnd()
                    isShow = !isShow
                }

                Log.d("TOUCH_SHOW_AFTER", "isShow : ${isShow}")
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

//    private fun setStroke(idx : Int) {
//        var row = 0
//        var col = 0
//        var id = ""
//        var resId = 0
//        for (i in 1..6) {
//            row = i
//            for (j in 1..7) {
//                col = j
//                id = "home_calendar_week_" + row + "_day_" + col
//                resId = requireActivity().resources.getIdentifier(id, "id", requireActivity().packageName)
//                var view = requireActivity().findViewById<View>(resId)
//                view.setBackgroundResource(0)
//            }
//        }
//
//        row = idx / 7 + 1
//        col = idx % 7 + 1
//        id = "home_calendar_week_" + row + "_day_" + col
//        resId = requireActivity().resources.getIdentifier(id, "id", requireActivity().packageName)
//        var select = requireActivity().findViewById<View>(R.id.home_calendar_week_2_day_2)
//
//        Log.d("RES_ID", "id : $id |  int id : $resId   | selectId : $select")
//        var view = requireActivity().findViewById<View>(resId)
//        view.background = requireActivity().resources.getDrawable(R.drawable.border_round_all_stroke_main_orange)
//    }

    private fun setAdapter() {
        binding.homeDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyEventRv.adapter = personalEventRVAdapter

        binding.homeDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyGroupEventRv.adapter = groupEventRVAdapter
        setToday()

        /** 기록 아이템 클릭 리스너 **/
        personalEventRVAdapter.setRecordClickListener(object :DailyPersonalRVAdapter.DiaryInterface{
            override fun onRecordClicked(event: Event) {
                val bundle=Bundle()
                bundle.putString("title",event.title)
                bundle.putInt("category",event.categoryColor)
                bundle.putString("place",event.place)
                bundle.putLong("date",event.startLong)

                val diaryFrag=DiaryDetailFragment()
                diaryFrag.arguments=bundle

                view?.findNavController()?.navigate(R.id.action_homeFragment_to_diaryDetailFragment2, bundle)

            }
        })
        /** ----- **/
    }


    private fun setToday() {
        monthList = getMonthList(DateTime(System.currentTimeMillis()))
        prevIdx = monthList.indexOf(DateTime(System.currentTimeMillis()).withTimeAtStartOfDay())
        nowIdx = prevIdx
        setDaily(nowIdx)
    }

    private fun setDaily(idx : Int) {
        binding.homeDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0,0)
        setData(idx)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData(idx : Int) {

        //getDummy(idx)

        getEvent(idx)

        personalEventRVAdapter.addPersonal(event_personal)
        groupEventRVAdapter.addGroup(event_group)
        setEmptyMsg()
    }

    private fun setEmptyMsg() {
        if (event_personal.size == 0 ) binding.homeDailyEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyEventNoneTv.visibility = View.GONE

        if (event_group.size == 0) binding.homeDailyGroupEventNoneTv.visibility = View.VISIBLE
        else binding.homeDailyGroupEventNoneTv.visibility = View.GONE
    }

    private fun getEvent(idx : Int) {
        event_personal.clear()
        event_group.clear()
        var todayStart = monthList[idx].withTimeAtStartOfDay().millis
        var todayEnd = monthList[idx].plusDays(1).withTimeAtStartOfDay().millis - 1

        var forPersonalEvent : Thread = Thread {
            event_personal = db.eventDao.getEventDaily(todayStart, todayEnd) as ArrayList<Event>
            personalEventRVAdapter.addPersonal(event_personal)
            requireActivity().runOnUiThread {
                personalEventRVAdapter.notifyDataSetChanged()
            }
        }
        forPersonalEvent.start()

        try {
            forPersonalEvent.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }


    }

//    private fun getDummy(idx : Int) {
//        event_personal.clear()
//        event_group.clear()
//        val date = monthList[idx]
//
//        event_personal.apply {
//            add(
//                Event(
//                    eventId = 1,
//                    "개인1",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(1,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
//                    R.color.palette1,
//                    1
//                )
//            )
//            add(
//                Event(
//                    "개인2",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(2,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,idx,0,0).millis),
//                    R.color.palette2,
//                    2
//                )
//            )
//            add(
//                Event(
//                    "개인3",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(3,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,idx,0,0).millis),
//                    R.color.palette3,
//                    3
//                )
//            )
//        }
//
//        event_group.apply {
//            add(
//                Event(
//                    "그룹4",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(1,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
//                    R.color.palette4,
//                    1
//                )
//            )
//            add(
//                Event(
//                    "그룹5",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(2,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
//                    R.color.palette5,
//                    2
//                )
//            )
//            add(
//                Event(
//                    "그룹6",
//                    date.withTimeAtStartOfDay().millis,
//                    date.withTime(3,date.dayOfMonth,0,0).millis,
//                    getInterval(date.withTimeAtStartOfDay().millis, date.withTime(5,date.dayOfMonth,0,0).millis),
//                    R.color.palette6,
//                    3
//                )
//            )
//        }
//    }

    private fun setMillisText() {
        binding.homeCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
        monthList = getMonthList(DateTime(millis))
        prevIdx = getPrevOffset(DateTime(millis).withDayOfMonth(1))
        nowIdx = prevIdx
        setDaily(nowIdx)
//        Log.d("PREV_IDX", prevIdx.toString())
//        Log.d("MONTH_LIST", monthList.toString())
    }

    private fun hideBottomNavigation( bool : Boolean){
        val bottomNavigationView : BottomNavigationView = requireActivity().findViewById(R.id.nav_bar)
        if(bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }
}