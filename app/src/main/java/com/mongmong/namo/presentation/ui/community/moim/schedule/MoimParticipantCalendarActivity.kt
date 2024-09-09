package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityMoimParticipantCalendarBinding
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.community.moim.calendar.adapter.ParticipantCalendarAdapter
import com.mongmong.namo.presentation.ui.group.GroupInfoActivity
import com.mongmong.namo.presentation.utils.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class MoimParticipantCalendarActivity : BaseActivity<ActivityMoimParticipantCalendarBinding>(R.layout.activity_moim_participant_calendar) {
    private lateinit var moimSchedule : Moim
    private lateinit var calendarAdapter : ParticipantCalendarAdapter

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1

    override fun setup() {
        moimSchedule = intent.getSerializableExtra("moim") as Moim
        setGroupInfo()

        calendarAdapter = ParticipantCalendarAdapter(this)
        binding.moimParticipantCalendarVp.apply{
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = calendarAdapter
            setCurrentItem(ParticipantCalendarAdapter.START_POSITION, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    pos = position
                    prevIdx = -1
                    millis = binding.moimParticipantCalendarVp.adapter!!.getItemId(position)
                    super.onPageSelected(position)
                }
            })
        }

        binding.moimParticipantCalendarYearMonthTv.text = DateTime(millis).toString("yyyy.MM")

        initClickListeners()
    }


    private fun initClickListeners() {
        //
        binding.moimParticipantCalendarYearMonthTv.setOnClickListener {
            SetMonthDialog(this, millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.moimParticipantCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }

        // 참석자 정보 표시
        binding.moimParticipantCalendarInfoIv.setOnClickListener {
            startActivity(Intent(this, GroupInfoActivity::class.java)
                .putExtra("group", moimSchedule)
            )
        }
    }

    private fun setGroupInfo() {
        binding.moimParticipantCalendarTitleTv.text = moimSchedule.title
    }

    fun getGroup() = moimSchedule

    companion object{
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null
    }
}