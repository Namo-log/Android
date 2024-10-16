package com.mongmong.namo.presentation.ui.community.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.data.dto.Period
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

class CalendarViewModel: ViewModel() {
    // 달력에 들어가는 한달치 날짜
    private val _monthDateList = MutableLiveData<List<DateTime>>()

    // 클릭한 날짜의 일정 처리
    private val _clickedDateTime = MutableLiveData<DateTime>()
    val clickedDateTime: LiveData<DateTime> = _clickedDateTime
    private lateinit var _dailyScheduleList: List<MoimScheduleBody> // 하루 일정

    // 친구 캘린더인지, 모임 캘린더인지를 구분
    var isFriendCalendar = true

    // 임시 친구 데이터
    lateinit var friend: Friend
    var friendCategoryList: List<Category>
    // 임시 모임 데이터
    var moim = Moim()

    init {
        moim = Moim(1, LocalDateTime.now(), "https://img.freepik.com/free-photo/beautiful-floral-composition_23-2150968962.jpg", "나모 모임 일정", "강남역",
            listOf(
                GroupMember(3, "코코아", 4),
                GroupMember(2, "짱구", 6),
            )
        )

        friendCategoryList = listOf(
            Category(name = "일정", colorId = 4),
            Category(name = "약속", colorId = 3),
            Category(name = "피자", colorId = 8),
        )
    }

    var isShow = false // 바텀 시트 표시 여부
    private var _prevIndex = -1 // 클릭한 날짜의 index
    private var _nowIndex = 0 // 클릭한 날짜의 index

    private fun setDailySchedule() {
        //TODO: 한 달 일정 중 선택 날짜에 해당되는 일정 필터링
    }

    // 캘린더의 날짜 클릭
    fun onClickCalendarDate(index: Int) {
        _nowIndex = index
        _clickedDateTime.value = getClickedDate() // 클릭한 날짜 저장
        setDailySchedule()
    }

    private fun getClickedDatePeriod(): SchedulePeriod {
        // 클릭한 날짜의 시작, 종료 시간
        return SchedulePeriod(
            getClickedDate().toLocalDateTime(), // 날짜 시작일
            getClickedDate() // 날짜 종료일
                .plusDays(1)
                .withTimeAtStartOfDay()
                .minusMillis(1)
                .toLocalDateTime()
        )
    }

    fun updateIsShow() {
        isShow = !isShow
        _prevIndex = _nowIndex
    }

    // 일정 상세 바텀 시트 닫기 - 동일한 날짜를 다시 클릭했을 경우
    fun isCloseScheduleDetailBottomSheet() = isShow && (_prevIndex == _nowIndex)

    // 캘린더에 들어갈 한달 날짜 리스트
    fun setMonthDayList(monthDayList: List<DateTime>) {
        _monthDateList.value = monthDayList
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDateList.value!![_nowIndex]
}