package com.mongmong.namo.presentation.ui.group.calendar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.databinding.FragmentGroupCalendarMonthBinding
import com.mongmong.namo.presentation.ui.group.diary.MoimDiaryActivity
import com.mongmong.namo.presentation.ui.group.GroupCalendarActivity
import com.mongmong.namo.presentation.ui.group.schedule.GroupScheduleActivity
import com.mongmong.namo.presentation.ui.group.calendar.adapter.GroupCalendarAdapter.Companion.GROUP_ID
import com.mongmong.namo.presentation.ui.group.calendar.adapter.GroupDailyMoimRVAdapter
import com.mongmong.namo.presentation.ui.group.calendar.adapter.GroupDailyPersonalRVAdapter
import com.mongmong.namo.presentation.ui.home.schedule.MoimScheduleViewModel
import com.mongmong.namo.presentation.utils.CustomCalendarView
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class GroupCalendarMonthFragment : Fragment() {

    private lateinit var binding : FragmentGroupCalendarMonthBinding
    private var groupId : Long = 0L
    private var yearMonth : String = ""

    private var millis : Long = 0L
    private var isShow = false
    private val scheduleList : ArrayList<MoimScheduleBody> = arrayListOf()
    private val dailySchedules : ArrayList<MoimScheduleBody> = arrayListOf()
    private val dailyGroupSchedules : ArrayList<MoimScheduleBody> = arrayListOf()
    private lateinit var monthDayList : List<DateTime>
    private var tempSchedule : ArrayList<MoimScheduleBody> = arrayListOf()

    private var prevIdx = -1
    private var nowIdx = 0

    private val personalScheduleRVAdapter = GroupDailyPersonalRVAdapter()
    private val groupScheduleRVAdapter = GroupDailyMoimRVAdapter()

    private val viewModel : MoimScheduleViewModel by viewModels()

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

        binding.groupCalendarMonthView.setDays(millis)
        monthDayList = binding.groupCalendarMonthView.days

        initClickListeners()
        initObserve()

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
        getGroupSchedules()
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

    private fun initClickListeners() {
        binding.groupFab.setOnClickListener {
            val intent = Intent(context, GroupScheduleActivity::class.java)
            intent.putExtra("nowDay", monthDayList[nowIdx].millis)
            intent.putExtra("group", (activity as GroupCalendarActivity).getGroup() )
            startActivity(intent)
        }

        // 날짜 상세보기
        binding.groupCalendarMonthView.onDateClickListener = object : CustomCalendarView.OnDateClickListener {
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
    }

    private fun setAdapter() {
        binding.groupDailyPersonalScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = personalScheduleRVAdapter
        }

        binding.groupDailyMoimScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = groupScheduleRVAdapter
        }

        groupScheduleRVAdapter.setMoimScheduleClickListener(object : GroupDailyMoimRVAdapter.MoimScheduleClickListener {
            override fun onContentClicked(groupSchedule: MoimScheduleBody) { // 아이템 전체 클릭
                val intent = Intent(context, GroupScheduleActivity::class.java)
                intent.putExtra("group", (activity as GroupCalendarActivity).getGroup())
                intent.putExtra("moimSchedule", groupSchedule)
                requireActivity().startActivity(intent)
            }

            override fun onDiaryIconClicked(groupSchedule: MoimScheduleBody) { // 기록 아이콘 클릭
                Log.d("GROUP_DIARY_CLICK", groupSchedule.toString())
                startActivity(Intent(context, MoimDiaryActivity::class.java)
                    .putExtra("from", "groupCalendar")
                    .putExtra("hasMoimActivity", groupSchedule.hasDiaryPlace)
                    .putExtra("moimScheduleId", groupSchedule.moimScheduleId)
                    .putExtra("moimSchedule", groupSchedule)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        })
    }

    private fun setDaily(dateId : Int) {
        binding.groupDailyHeaderTv.text = monthDayList[dateId].toString("MM.dd (E)")
        binding.groupScrollSv.scrollTo(0,0)
        setDailyData(dateId)
    }

    private fun setDailyData(dateId : Int) {
        getSchedule(dateId) // 오늘 일정 가져오기
        setEmptyText()
    }

    private fun setEmptyText() {
        with(binding) {
            // 개인 일정
            groupDailyPersonalScheduleNoneTv.visibility = if (dailySchedules.size == 0) View.VISIBLE else View.GONE
            // 모임 일정
            groupDailyMoimScheduleNoneTv.visibility = if (dailyGroupSchedules.size == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getSchedule(dateId : Int) {
        val todayStart = monthDayList[dateId].withTimeAtStartOfDay().millis
        val todayEnd = monthDayList[dateId].plusDays(1).withTimeAtStartOfDay().millis - 1

        tempSchedule.clear()
        tempSchedule.addAll(scheduleList.filter { schedule ->
            schedule.startDate <= todayEnd / 1000 && schedule.endDate >= todayStart / 1000
        })

        dailySchedules.clear()
        dailyGroupSchedules.clear()
        dailySchedules.addAll(
            tempSchedule.filter { event ->
                !event.curMoimSchedule
            }
        )
        dailyGroupSchedules.addAll(
            tempSchedule.filter { event ->
                event.curMoimSchedule
            }
        )
        Log.d("GroupCalMonFrag", dailySchedules.toString())
        Log.d("GroupCalMonFrag", "Group Schedule : " + dailyGroupSchedules.toString())

        personalScheduleRVAdapter.addPersonal(dailySchedules)
        groupScheduleRVAdapter.addGroupSchedule(dailyGroupSchedules)
    }

    private fun initObserve() {
        viewModel.groupScheduleList.observe(viewLifecycleOwner) { result ->
            scheduleList.clear()
            if (!result.isNullOrEmpty()) {
                scheduleList.addAll(filterMonthSchedule(result as ArrayList))
                drawMonthCalendar(scheduleList)
            }
        }
    }

    private fun filterMonthSchedule(scheduleList: ArrayList<MoimScheduleBody>): List<MoimScheduleBody> {
        val monthStart = monthDayList[0].withTimeAtStartOfDay().millis / 1000
        val monthEnd = monthDayList[41].plusDays(1).withTimeAtStartOfDay().millis / 1000
        return scheduleList.filter { schedule ->
            schedule.startDate <= monthEnd && schedule.endDate >= monthStart
        }
    }

    private fun drawMonthCalendar(scheduleList: ArrayList<MoimScheduleBody>) {
        binding.groupCalendarMonthView.setScheduleList(scheduleList)
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

    /** 그룹의 일정 전체 조회 (그룹원 개인 및 모임 일정) */
    private fun getGroupSchedules() {
        viewModel.getGroupAllSchedules(groupId)
    }

    companion object {
        private const val MILLIS = "MILLIS"

        fun newInstance(millis : Long) = GroupCalendarMonthFragment().apply {
            arguments = Bundle().apply {
                putLong(MILLIS, millis)
            }
        }
    }
}