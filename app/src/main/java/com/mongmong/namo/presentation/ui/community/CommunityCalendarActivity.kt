package com.mongmong.namo.presentation.ui.community

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityCommunityCalendarBinding
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.community.calendar.adapter.CommunityCalendarAdapter
import com.mongmong.namo.presentation.ui.community.calendar.CalendarInfoDialog
import com.mongmong.namo.presentation.ui.community.calendar.CalendarViewModel
import com.mongmong.namo.presentation.utils.SetMonthDialog
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class CommunityCalendarActivity : BaseActivity<ActivityCommunityCalendarBinding>(R.layout.activity_community_calendar) {

    private val viewModel: CalendarViewModel by viewModels()

    private lateinit var calendarAdapter : CommunityCalendarAdapter

    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis
    private var pos = Int.MAX_VALUE / 2

    private var prevIdx = -1

    override fun setup() {
        initViews()
        initClickListeners()
    }

    private fun initViews() {
        viewModel.isFriendCalendar = intent.getBooleanExtra("isFriendCalendar", true)

        if (viewModel.isFriendCalendar) { // 친구 정보 세팅
            viewModel.friend = intent.getSerializableExtra("friend") as Friend
            setCalendarTitleInfo(viewModel.friend.nickname)
        } else { // 모임 정보 세팅
            viewModel.moimSchedule = intent.getSerializableExtra("moim") as MoimScheduleDetail
            setCalendarTitleInfo(viewModel.moimSchedule.title)
        }

        calendarAdapter = CommunityCalendarAdapter(this)

        binding.communityCalendarVp.apply{
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = calendarAdapter
            setCurrentItem(CommunityCalendarAdapter.START_POSITION, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    pos = position
                    prevIdx = -1
                    millis = binding.communityCalendarVp.adapter!!.getItemId(position)
                    binding.communityCalendarYearMonthTv.text = DateTime(millis).toString(YEAR_MONTH_PATTERN)
                    super.onPageSelected(position)
                }
            })
        }

        binding.communityCalendarYearMonthTv.text = DateTime(millis).toString(YEAR_MONTH_PATTERN)
    }


    private fun initClickListeners() {
        // 뒤로가기 버튼 클릭
        binding.communityCalendarBackIv.setOnClickListener {
            finish() // 캘린더 닫기
        }

        // 연도 선택
        binding.communityCalendarYearMonthTv.setOnClickListener {
            SetMonthDialog(this, millis) {
                val date = it
                var result = 0
                result = (date.year - DateTime(millis).year) * 12 + (date.monthOfYear - DateTime(millis).monthOfYear)
                binding.communityCalendarVp.setCurrentItem(pos + result, true)
            }.show()
        }

        // 캘린더 정보 표시
        binding.communityCalendarInfoIv.setOnClickListener {
            CalendarInfoDialog().show(this.supportFragmentManager, "ParticipantDialog")
        }
    }

    private fun setCalendarTitleInfo(title: String) {
        binding.communityCalendarTitleTv.text = title
    }

    companion object{
        var currentFragment : Fragment? = null
        var currentSelectedPos : Int? = null
        var currentSelectedDate : DateTime? = null

        const val YEAR_MONTH_PATTERN = "yyyy.MM"
    }
}