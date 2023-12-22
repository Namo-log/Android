package com.example.namo.ui.bottom.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.data.NamoDatabase
import com.example.namo.data.remote.moim.Moim
import com.example.namo.databinding.ActivityGroupCalendarBinding
import com.example.namo.ui.bottom.group.calendar.GroupCalendarAdapter
import com.example.namo.ui.bottom.group.calendar.GroupCalendarAdapter.Companion.GROUP_ID
import com.example.namo.ui.bottom.home.calendar.SetMonthDialog
import org.joda.time.DateTime

class GroupCalendarActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGroupCalendarBinding
    private lateinit var db : NamoDatabase
    private lateinit var group : Moim
    private lateinit var calendarAdapter : GroupCalendarAdapter

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var toadyPos = Int.MAX_VALUE / 2
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1
    private var nowIdx = 0

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
        db = NamoDatabase.getInstance(this)
        setContentView(binding.root)

        group = intent.getSerializableExtra("moim") as Moim
        GROUP_ID = group.groupId
        setGroupInfo()

        calendarAdapter = GroupCalendarAdapter(this)
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

    fun getGroup() : Moim {
        return group
    }

    companion object{
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}