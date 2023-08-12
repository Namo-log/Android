package com.example.namo.ui.bottom.group.calendar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.moim.Moim
import com.example.namo.databinding.FragmentGroupCalendarBinding
import com.example.namo.ui.bottom.group.GroupInfoActivity
import com.example.namo.ui.bottom.group.calendar.GroupCalendarAdapter.Companion.GROUP_ID
import com.example.namo.ui.bottom.home.adapter.DailyGroupRVAdapter
import com.example.namo.ui.bottom.home.adapter.DailyPersonalRVAdapter
import com.example.namo.ui.bottom.home.calendar.SetMonthDialog
import com.example.namo.utils.CalendarUtils
import org.joda.time.DateTime

class GroupCalendarFragment : Fragment() {

    private lateinit var calendarAdapter : GroupCalendarAdapter
    private lateinit var binding: FragmentGroupCalendarBinding
    private val args : GroupCalendarFragmentArgs by navArgs()

    private lateinit var group : Moim
    private lateinit var monthList : List<DateTime>

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private val todayPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1
    private var nowIdx = 0

    lateinit var db : NamoDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_calendar, container, false)
        db = NamoDatabase.getInstance(requireContext())

        //그룹 정보 저장
        group = args.group
        GROUP_ID = group.groupId
        setGroupInfo()

        calendarAdapter = GroupCalendarAdapter(context as MainActivity)

        binding.groupCalendarVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.groupCalendarVp.adapter = calendarAdapter
        binding.groupCalendarVp.setCurrentItem(GroupCalendarAdapter.START_POSITION, false)
        binding.groupCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")

        binding.groupCalendarVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pos = position
                prevIdx = -1
                millis = binding.groupCalendarVp.adapter!!.getItemId(position)
                binding.groupCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
                super.onPageSelected(position)
            }
        })

        clickListener()

        return binding.root
    }

    private fun clickListener() {
        binding.groupCalendarInfoIv.setOnClickListener {
            val intent = Intent(context, GroupInfoActivity::class.java)
            intent.putExtra("group", group)
            launcher.launch(intent)
        }

        binding.groupCalendarYearMonthTv.setOnClickListener {
            SetMonthDialog(requireContext(), millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.groupCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }
    }

    private val launcher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                view?.findNavController()?.navigate(R.id.action_groupCalendarFragment_to_groupListFragment)
            }
        }

    private fun setGroupInfo() {
        binding.groupCalendarGroupTitleTv.text = group.groupName
    }

    fun getGroupId() : Long {
        return group.groupId
    }

    companion object{
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}