package com.example.namo.ui.bottom.group.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.moim.GetMoimScheduleResponse
import com.example.namo.data.remote.moim.GetMoimScheduleView
import com.example.namo.data.remote.moim.MoimSchedule
import com.example.namo.data.remote.moim.MoimService
import com.example.namo.databinding.FragmentGroupCalendarMonthBinding
import com.example.namo.ui.bottom.diary.groupDiary.GroupModifyActivity
import com.example.namo.ui.bottom.group.GroupCalendarActivity
import com.example.namo.ui.bottom.group.GroupScheduleActivity
import com.example.namo.ui.bottom.group.calendar.GroupCalendarAdapter.Companion.GROUP_ID
import com.example.namo.ui.bottom.group.calendar.adapter.GroupDailyGroupRVAdapter
import com.example.namo.ui.bottom.group.calendar.adapter.GroupDailyPersonalRVAdapter
import com.example.namo.ui.bottom.home.calendar.CustomCalendarView
import com.example.namo.utils.NetworkManager
import org.joda.time.DateTime

class GroupCalendarMonthFragment : Fragment(), GetMoimScheduleView {
    lateinit var db : NamoDatabase
    private lateinit var binding : FragmentGroupCalendarMonthBinding
    private var groupId : Long = 0L
    private var yearMonth : String = ""

    private var millis : Long = 0L
    private var isShow = false
    private val eventList : ArrayList<MoimSchedule> = arrayListOf()
    private val dailyEvents : ArrayList<MoimSchedule> = arrayListOf()
    private val dailyGroupEvents : ArrayList<MoimSchedule> = arrayListOf()
    private lateinit var monthList : List<DateTime>
    private var tempEvent : ArrayList<MoimSchedule> = arrayListOf()

    private var prevIdx = -1
    private var nowIdx = 0
    private var event_personal : ArrayList<Event> = arrayListOf()
    private var event_group : ArrayList<Event> = arrayListOf()
    private val personalEventRVAdapter = GroupDailyPersonalRVAdapter()
    private val groupEventRVAdapter = GroupDailyGroupRVAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            millis = it.getLong(MILLIS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupCalendarMonthBinding.inflate(inflater, container, false)
        db = NamoDatabase.getInstance(requireContext())

        binding.groupCalendarMonthView.setDayList(millis)
        monthList = binding.groupCalendarMonthView.getDayList()

        binding.groupFab.setOnClickListener {
            val intent = Intent(context, GroupScheduleActivity::class.java)
            intent.putExtra("nowDay", monthList[nowIdx].millis)
            intent.putExtra("group", (activity as GroupCalendarActivity).getGroup() )
            startActivity(intent)
        }

        binding.groupCalendarMonthView.onDateClickListener = object : GroupCustomCalendarView.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos: Int?) {
                val prevFragment = GroupCalendarActivity.currentFragment as GroupCalendarMonthFragment?
                if (prevFragment != null && prevFragment != this@GroupCalendarMonthFragment) {
                    prevFragment.binding.groupCalendarMonthView.selectedDate = null
                    prevFragment.binding.constraintLayout2.transitionToStart()
                }

                GroupCalendarActivity.currentFragment = this@GroupCalendarMonthFragment
                GroupCalendarActivity.currentSelectedPos = pos
                GroupCalendarActivity.currentSelectedDate = date

                binding.groupCalendarMonthView.selectedDate = date

                if (date != null && pos != null) {
                    nowIdx = pos
                    setDaily(nowIdx)

                    if (isShow && prevIdx == nowIdx) {
                        binding.constraintLayout2.transitionToStart()
                        binding.groupCalendarMonthView.selectedDate = null
                        GroupCalendarActivity.currentFragment = null
                        GroupCalendarActivity.currentSelectedPos = null
                        GroupCalendarActivity.currentSelectedDate = null
                    }
                    else if (!isShow) {
                        binding.constraintLayout2.transitionToEnd()
                    }
                    isShow = !isShow
                    prevIdx = nowIdx
                }

                binding.groupCalendarMonthView.invalidate()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = GROUP_ID
        yearMonth = DateTime(millis).toString("yyyy,MM")
        Log.d("GroupCalMonFrag", "Group ID : $groupId")
        Log.d("GroupCalMonFrag", "YearMonth : ${DateTime(millis).toString("yyyy,MM")} ")
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
        getGroupSchedule()
    }

    override fun onPause() {
        super.onPause()
        val listener = object : CustomCalendarView.OnDateClickListener {
            override fun onDateClick(date: DateTime?, pos : Int?) {
                binding.groupCalendarMonthView.selectedDate = null
                binding.constraintLayout2.transitionToStart()
                isShow = false
                binding.constraintLayout2.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    private fun setAdapter() {
        binding.groupDailyEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyEventRv.adapter = personalEventRVAdapter

        binding.groupDailyGroupEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.groupDailyGroupEventRv.adapter = groupEventRVAdapter

        groupEventRVAdapter.setContentClickListener(object : GroupDailyGroupRVAdapter.ContentClickListener {
            override fun onContentClick(groupSchedule: MoimSchedule) {
                val intent = Intent(context, GroupScheduleActivity::class.java)
                intent.putExtra("group", (activity as GroupCalendarActivity).getGroup())
                intent.putExtra("moimSchedule", groupSchedule)
                requireActivity().startActivity(intent)
            }
        })

        groupEventRVAdapter.setRecordClickListener(object :GroupDailyGroupRVAdapter.DiaryInterface{
            override fun onGroupMemoClicked(groupEvent: MoimSchedule) {
                val intent = Intent(context, GroupModifyActivity::class.java)
                intent.putExtra("groupEvent", groupEvent)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                requireActivity().startActivity(intent)

            }
        })
    }

    private fun setDaily(idx : Int) {
        binding.groupDailyHeaderTv.text = monthList[idx].toString("MM.dd (E)")
        binding.groupScrollSv.scrollTo(0,0)
        setData(idx)
    }

    private fun setData(idx : Int) {
        getEvent(idx)
        setEmptyMsg()
    }

    private fun setEmptyMsg() {
        if (dailyEvents.size == 0 ) binding.groupDailyEventNoneTv.visibility = View.VISIBLE
        else binding.groupDailyEventNoneTv.visibility = View.GONE
    }

    private fun getEvent(idx : Int) {
        var todayStart = monthList[idx].withTimeAtStartOfDay().millis
        var todayEnd = monthList[idx].plusDays(1).withTimeAtStartOfDay().millis - 1

        tempEvent.clear()
        tempEvent.addAll(eventList.filter { event ->
            event.startDate <= todayEnd / 1000 && event.endDate >= todayStart / 1000
        })

        dailyEvents.clear()
        dailyGroupEvents.clear()
        dailyEvents.addAll(
            tempEvent.filter { event ->
                !event.curMoimSchedule
            }
        )
        dailyGroupEvents.addAll(
            tempEvent.filter { event ->
                event.curMoimSchedule
            }
        )
        Log.d("GroupCalMonFrag", dailyEvents.toString())
        Log.d("GroupCalMonFrag", "Group Schedule : " + dailyGroupEvents.toString())

        personalEventRVAdapter.addPersonal(dailyEvents)
        groupEventRVAdapter.addGroupSchedule(dailyGroupEvents)


    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = GroupCalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }


    // API 관련
    private fun getGroupSchedule() {
        if (!NetworkManager.checkNetworkState(requireContext())) {
            Toast.makeText(context, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val moimService = MoimService()
        moimService.setGetMoimScheduleView(this)

        moimService.getMoimSchedule(groupId, yearMonth)
    }

    override fun onGetMoimScheduleSuccess(response: GetMoimScheduleResponse) {
        Log.d("GroupCalMonFrag", "onGetMoimScheduleSuccess")
        Log.d("GroupCalMonFrag", response.result.toString())
        eventList.clear()
        eventList.addAll(response.result)
        binding.groupCalendarMonthView.setEventList(eventList)
        binding.groupCalendarMonthView.invalidate()


        if (GroupCalendarActivity.currentFragment == null) {
            return
        }
        else if (this@GroupCalendarMonthFragment != GroupCalendarActivity.currentFragment) {
            isShow = false
            prevIdx = -1
        } else {
            binding.groupCalendarMonthView.selectedDate = GroupCalendarActivity.currentSelectedDate
            nowIdx = GroupCalendarActivity.currentSelectedPos!!
            setDaily(nowIdx)
            binding.constraintLayout2.transitionToEnd()
            isShow = true
            prevIdx = nowIdx
        }
    }

    override fun onGetMoimScheduleFailure(message: String) {
        Log.d("GroupCalMonFrag", "onGetMoimScheduleFailure")
    }
}