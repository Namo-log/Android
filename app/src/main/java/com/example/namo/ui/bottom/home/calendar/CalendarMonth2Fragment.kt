package com.example.namo.ui.bottom.home.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.FragmentCalendarMonth2Binding
import com.example.namo.ui.bottom.diary.DiaryAddFragment
import com.example.namo.ui.bottom.diary.DiaryModifyFragment
import com.example.namo.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.ui.bottom.home.schedule.ScheduleDialogFragment
import com.example.namo.utils.CalendarUtils
import org.joda.time.DateTime

class CalendarMonth2Fragment : Fragment() {
    lateinit var db : NamoDatabase
    private lateinit var binding : FragmentCalendarMonth2Binding

    private var millis : Long = 0L
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2
    private var isShow = false
    private lateinit var monthList : List<DateTime>
    private lateinit var tempEvent : List<Event>

    private var prevIdx = -1
    private var nowIdx = 0
    private var event_personal : ArrayList<Event> = arrayListOf()
    private var event_group : ArrayList<Event> = arrayListOf()
    private val personalEventRVAdapter = DailyPersonalRVAdapter()
    private val groupEventRVAdapter = DailyGroupRVAdapter()

    private lateinit var scheduleDialogFragment : ScheduleDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            millis = it.getLong(MILLIS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarMonth2Binding.inflate(inflater, container, false)
        db = NamoDatabase.getInstance(requireContext())

//        binding.calendarHeader.text = DateTime(millis).toString("yyyy.MM")
        binding.calendarMonthView.setDayList(millis)
        monthList = binding.calendarMonthView.getDayList()

        var forDB : Thread = Thread {
            tempEvent = db.eventDao.getEventMonth(monthList[0].withTimeAtStartOfDay().millis, monthList[41].plusDays(1).withTimeAtStartOfDay().millis)
        }
        forDB.start()
        try {
            forDB.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
        binding.calendarMonthView.setEventList(tempEvent)

        binding.homeFab.setOnClickListener {
            Log.d("DIALOG_OPEN", nowIdx.toString())
            scheduleDialogFragment = ScheduleDialogFragment {
                Log.d("DIALOG_CALLBACK", it.toString())
                if (it) {
                    setData(nowIdx)
                    Log.d("GET_EVENT", nowIdx.toString())
                }

                var page : Fragment = this@CalendarMonth2Fragment
                page.onResume()

                Log.d("DIALOG_CLOSE", nowIdx.toString())
                setDaily(nowIdx)
            }
            scheduleDialogFragment.setDate(monthList[nowIdx])
            scheduleDialogFragment.show(requireActivity().supportFragmentManager, ScheduleDialogFragment.TAG)
        }

        binding.calendarMonthView.onDateClickListener = object : Calendar2View.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos : Int?) {
                if (date == null) Log.d("CALENDAR_FRAGMENT", "The NULL clicked!")
                Log.d("CALENDAR_FRAGMENT", "THE $date is clicked!")
                binding.calendarMonthView.selectedDate = date

                if (date != null && pos != null) {
                    nowIdx = pos
                    setDaily(nowIdx)

                    if (isShow && prevIdx == nowIdx) {
                        binding.constraintLayout.transitionToStart()
                        isShow = !isShow
                    }
                    else if (!isShow) {
                        binding.constraintLayout.transitionToEnd()
                        isShow = !isShow
                    }
                    prevIdx = nowIdx
                }

                binding.calendarMonthView.invalidate()
            }
        }

        return binding.root
    }

    private fun setAdapter() {
        binding.homeDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyEventRv.adapter = personalEventRVAdapter

        binding.homeDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.homeDailyGroupEventRv.adapter = groupEventRVAdapter
//        if (nowIdx==0) setToday()

        personalEventRVAdapter.setContentClickListener(object : DailyPersonalRVAdapter.ContentClickListener {
            override fun onContentClick(event: Event) {
                scheduleDialogFragment = ScheduleDialogFragment {
                    Log.d("DIALOG_CALLBACK", it.toString())
                    if (it) {
                        setData(nowIdx)
                        Log.d("GET_EVENT", nowIdx.toString())
                    }

                    var page : Fragment = this@CalendarMonth2Fragment
                    page.onResume()

                    setDaily(nowIdx)
                }
                scheduleDialogFragment.isEdit = true
                scheduleDialogFragment.setEvent(event)
                scheduleDialogFragment.show(requireActivity().supportFragmentManager, ScheduleDialogFragment.TAG)
            }

        })

        /** 기록 아이템 클릭 리스너 **/
        personalEventRVAdapter.setRecordClickListener(object : DailyPersonalRVAdapter.DiaryInterface{
            override fun onAddClicked(event: Event) {
                val bundle=Bundle()
                bundle.putInt("scheduleIdx",event.eventId)
                bundle.putString("title",event.title)
                bundle.putInt("category",event.categoryColor)
                bundle.putString("place",event.place)
                bundle.putLong("date",event.startLong)

                val diaryFrag= DiaryAddFragment()
                diaryFrag.arguments=bundle

                view?.findNavController()?.navigate(R.id.action_homeFragment_to_diaryDetailFragment2, bundle)
            }
            override fun onEditClicked(event: Event) {
                val bundle=Bundle()
                bundle.putInt("scheduleIdx",event.eventId)

                val editFrag= DiaryModifyFragment()
                editFrag.arguments=bundle
                view?.findNavController()?.navigate(R.id.action_homeFragment_to_diaryModifyFragment,bundle)
            }
        })
        /** ----- **/
    }

    private fun setToday() {
        prevIdx = monthList.indexOf(DateTime(System.currentTimeMillis()).withTimeAtStartOfDay())
        nowIdx = prevIdx
        setDaily(nowIdx)
    }

    private fun setDaily(idx : Int) {
        binding.homeDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.dailyScrollSv.scrollTo(0,0)
        setData(idx)
    }

    private fun setData(idx : Int) {
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getEvent(idx : Int) {
        event_personal.clear()
        event_group.clear()
        var todayStart = monthList[idx].withTimeAtStartOfDay().millis
        var todayEnd = monthList[idx].plusDays(1).withTimeAtStartOfDay().millis - 1

        var forPersonalEvent : Thread = Thread {
            event_personal = db.eventDao.getEventDaily(todayStart, todayEnd) as ArrayList<Event>
            personalEventRVAdapter.addPersonal(event_personal)
            requireActivity().runOnUiThread {
                Log.d("NOTIFY", event_personal.toString())
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

    override fun onResume() {
        super.onResume()
        setAdapter()
        Log.d("CalendarMonth2", "OnResume")
        Log.d("CALENDAR_CHECK", binding.calendarMonthView.getDayList().toString())
        Log.d("CALENDAR_CHECK", binding.calendarMonthView.getEventList().toString())
    }

    override fun onPause() {
        super.onPause()
        Log.d("CalendarMonth2", "OnPause")
        val listener = object : Calendar2View.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos : Int?) {
                binding.calendarMonthView.selectedDate = null
                binding.constraintLayout.transitionToStart()
                isShow = false
                binding.calendarMonthView.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = CalendarMonth2Fragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}