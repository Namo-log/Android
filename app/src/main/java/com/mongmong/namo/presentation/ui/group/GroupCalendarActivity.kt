package com.mongmong.namo.presentation.ui.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.databinding.ActivityGroupCalendarBinding
import com.mongmong.namo.presentation.ui.group.calendar.adapter.GroupCalendarAdapter
import com.mongmong.namo.presentation.ui.group.calendar.adapter.GroupCalendarAdapter.Companion.GROUP_ID
import com.mongmong.namo.presentation.utils.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class GroupCalendarActivity : AppCompatActivity() {
    private lateinit var binding : ActivityGroupCalendarBinding
    private lateinit var group : Group
    private lateinit var calendarAdapter : GroupCalendarAdapter

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1

    private val getResultValue = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사용자 탈퇴 여부
            val returnLeave = result.data?.getBooleanExtra("leave", false)
            if (returnLeave == true) { // 사용자가 탈퇴했다면
                finish() // 그룹 캘린더 화면 나가기
            }
            // 그룹명 변경 여부
            val groupName = result.data?.getStringExtra("groupName")
            if (groupName?.isNotEmpty() == true) {
                binding.groupCalendarGroupTitleTv.text = groupName
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        group = intent.getSerializableExtra("moim") as Group
        GROUP_ID = group.groupId
        setGroupInfo()

        calendarAdapter = GroupCalendarAdapter(this)
        binding.groupCalendarVp.apply{
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = calendarAdapter
            setCurrentItem(GroupCalendarAdapter.START_POSITION, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    pos = position
                    prevIdx = -1
                    millis = binding.groupCalendarVp.adapter!!.getItemId(position)
                    binding.groupCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
                    super.onPageSelected(position)
                }
            })
        }

        binding.groupCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")
    }

    override fun onStart() {
        super.onStart()

        clickListener()
    }

    private fun clickListener() {
        binding.groupCalendarInfoIv.setOnClickListener {

            val intent = Intent(this, GroupInfoActivity::class.java)
            intent.putExtra("group", group)
            // GropInfoActivity에서 넘겨준 사용자 탈퇴 여부
            getResultValue.launch(intent)
        }

        binding.groupCalendarYearMonthLayout.setOnClickListener {
            SetMonthDialog(this, millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.groupCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }
    }

    private fun setGroupInfo() {
        binding.groupCalendarGroupTitleTv.text = group.groupName
    }

    fun getGroup() = group

    companion object{
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}