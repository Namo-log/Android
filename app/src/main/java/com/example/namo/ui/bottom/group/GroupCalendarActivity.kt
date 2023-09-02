package com.example.namo.ui.bottom.group

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.data.NamoDatabase
import com.example.namo.data.remote.moim.Moim
import com.example.namo.data.remote.moim.MoimListUser
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

        clickListener()
    }

    private fun clickListener() {
        binding.groupCalendarInfoIv.setOnClickListener {
            val intent = Intent(this, GroupInfoActivity::class.java)
            intent.putExtra("group", group)
            startActivity(intent)
        }

        binding.groupCalendarYearMonthTv.setOnClickListener {
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