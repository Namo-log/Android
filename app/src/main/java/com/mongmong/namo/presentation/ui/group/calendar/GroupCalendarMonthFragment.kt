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
import com.mongmong.namo.presentation.ui.group.schedule.MoimScheduleViewModel
import com.mongmong.namo.presentation.utils.CustomCalendarView
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class GroupCalendarMonthFragment : Fragment() {

    private lateinit var binding : FragmentGroupCalendarMonthBinding

    private var millis : Long = 0L

    private val personalDailyScheduleAdapter = GroupDailyPersonalRVAdapter()
    private val groupDailyScheduleAdapter = GroupDailyMoimRVAdapter()

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

        binding.apply {
            viewModel = this@GroupCalendarMonthFragment.viewModel
            lifecycleOwner = this@GroupCalendarMonthFragment
        }

        binding.groupCalendarMonthView.setDays(millis)
        viewModel.setMonthDayList(binding.groupCalendarMonthView.days)

        initClickListeners()
        initObserve()
        initAdapter()

        return binding.root
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
                viewModel.setIsShow(false)
                binding.constraintLayout2.invalidate()
            }
        }
        listener.onDateClick(null, null)
    }

    private fun initClickListeners() {
        binding.groupFab.setOnClickListener {
            val intent = Intent(context, GroupScheduleActivity::class.java)
            intent.putExtra("nowDay", viewModel.getClickedDate().millis)
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
                    // 클릭 처리
                    personalDailyScheduleAdapter.setClickedDate(date)
                    groupDailyScheduleAdapter.setClickedDate(date)

                    viewModel.clickDate(pos)
                    setDailySchedule()

                    if (viewModel.isCloseScheduleDetailBottomSheet()) {
                        binding.constraintLayout2.transitionToStart()
                        binding.groupCalendarMonthView.selectedDate = null
                        GroupCalendarActivity.currentFragment = null
                        GroupCalendarActivity.currentSelectedPos = null
                        GroupCalendarActivity.currentSelectedDate = null
                    } else if (viewModel.isShow.value == false) { // 바텀시트 닫기
                        binding.constraintLayout2.transitionToEnd()
                    }
                    viewModel.updateIsShow()
                }

                binding.groupCalendarMonthView.invalidate()
            }
        }
    }

    private fun initAdapter() {
        personalDailyScheduleAdapter.initScheduleTimeConverter()
        groupDailyScheduleAdapter.initScheduleTimeConverter()
    }

    private fun setAdapter() {
        binding.groupDailyPersonalScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = personalDailyScheduleAdapter
        }

        binding.groupDailyMoimScheduleRv.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = groupDailyScheduleAdapter
        }

        groupDailyScheduleAdapter.setScheduleClickListener(object : GroupDailyMoimRVAdapter.MoimScheduleClickListener {
            override fun onContentClicked(groupSchedule: MoimScheduleBody) { // 아이템 전체 클릭
                requireActivity().startActivity(Intent(context, GroupScheduleActivity::class.java)
                    .putExtra("group", (activity as GroupCalendarActivity).getGroup())
                    .putExtra("moimSchedule", groupSchedule)
                )
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

    private fun setDailySchedule() {
        binding.groupScrollSv.scrollTo(0,0)
        // 일정 아이템 표시
        personalDailyScheduleAdapter.addPersonal(viewModel.getDailySchedules(false))
        groupDailyScheduleAdapter.addGroupSchedule(viewModel.getDailySchedules(true))
    }

    private fun initObserve() {
        viewModel.groupScheduleList.observe(viewLifecycleOwner) { result ->
            viewModel.filterMonthSchedule()
            if (!result.isNullOrEmpty()) {
                drawMonthCalendar(viewModel.monthScheduleList.value!! as ArrayList<MoimScheduleBody>)
            }
        }
    }

    private fun drawMonthCalendar(scheduleList: ArrayList<MoimScheduleBody>) {
        binding.groupCalendarMonthView.setScheduleList(scheduleList)
        binding.groupCalendarMonthView.invalidate()

        if (GroupCalendarActivity.currentFragment == null) {
            return
        } else if (this@GroupCalendarMonthFragment == GroupCalendarActivity.currentFragment) {
            binding.groupCalendarMonthView.selectedDate = GroupCalendarActivity.currentSelectedDate
            viewModel.clickDate(GroupCalendarActivity.currentSelectedPos!!)
            setDailySchedule()
            binding.constraintLayout2.transitionToEnd()
            viewModel.updateIsShow()
        }
    }

    /** 그룹의 일정 전체 조회 (그룹원 개인 및 모임 일정) */
    private fun getGroupSchedules() {
        viewModel.getGroupAllSchedules(GROUP_ID)
        Log.d("GroupCalMonFrag", "Group ID : $GROUP_ID")
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