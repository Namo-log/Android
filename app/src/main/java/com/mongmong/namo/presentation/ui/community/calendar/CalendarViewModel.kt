package com.mongmong.namo.presentation.ui.community.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.CalendarColorInfo
import com.mongmong.namo.domain.model.CommunityCommonSchedule
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.FriendSchedule
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.domain.model.ScheduleType
import com.mongmong.namo.domain.repositories.FriendRepository
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor (
    private val moimRepository: ScheduleRepository,
    private val friendRepository: FriendRepository
): ViewModel() {
    // 친구 캘린더인지, 모임 캘린더인지를 구분
    var isFriendCalendar = true

    // 달력에 들어가는 한달치 날짜
    private var _monthDateList: List<DateTime> = emptyList()

    // 모임 캘린더 일정
    private val _moimScheduleList = MutableLiveData<List<MoimCalendarSchedule>>()
    val moimScheduleList: LiveData<List<MoimCalendarSchedule>> = _moimScheduleList

    // 친구 캘린더 일정
    private val _friendScheduleList = MutableLiveData<List<FriendSchedule>>()
    val friendScheduleList: LiveData<List<FriendSchedule>> = _friendScheduleList

    // 클릭한 날짜의 일정 처리
    private val _clickedDateTime = MutableLiveData<DateTime>()
    val clickedDateTime: LiveData<DateTime> = _clickedDateTime

    private var _dailyScheduleList: List<CommunityCommonSchedule> = emptyList() // 하루 일정

    private val _isMoimScheduleExist = MutableLiveData<Boolean>() // 모임 일정 정보가 있을 경우
    val isMoimScheduleExist: LiveData<Boolean> = _isMoimScheduleExist

    private val _isParticipantScheduleEmpty = MutableLiveData<Boolean>() // 친구/참석자 일정이 있을 경우
    var isParticipantScheduleEmpty: LiveData<Boolean> = _isParticipantScheduleEmpty

    var moimSchedule = MoimScheduleDetail()
    lateinit var friend: Friend
    var friendCategoryList: List<CalendarColorInfo> = emptyList()

    var isShowDailyBottomSheet: Boolean = false

    private var _prevIndex = -1 // 이전에 클릭한 날짜의 index
    private var _nowIndex = 0 // 현재 클릭한 날짜의 index

    /** 모임 캘린더 일정 조회 */
    fun getMoimCalendarSchedules() {
        viewModelScope.launch {
            // 범위로 일정 목록 조회
            _moimScheduleList.value = moimRepository.getMoimCalendarSchedules(
                moimScheduleId = moimSchedule.moimId,
                startDate = _monthDateList.first(), // 캘린더에 표시되는 첫번쨰 날짜
                endDate = _monthDateList.last() // 캘린더에 표시되는 마지막 날짜
            )
        }
    }

    /** 친구 일정 조회 */
    fun getFriendCalendarSchedules() {
        viewModelScope.launch {
            // 범위로 일정 목록 조회
            _friendScheduleList.value = friendRepository.getFriendCalendar(
                userId = friend.userId,
                startDate = _monthDateList.first(), // 캘린더에 표시되는 첫번쨰 날짜
                endDate = _monthDateList.last() // 캘린더에 표시되는 마지막 날짜
            )
        }
    }

    /** 친구 카테고리 조회 */
    fun getFriendCategories() {
        viewModelScope.launch {
            friendCategoryList = friendRepository.getFriendCategoryList(friend.userId)
        }
    }

    private fun setDailySchedule() {
        _dailyScheduleList = getFilteredScheduleList() // 일정 필터링
        _isParticipantScheduleEmpty.value = isDailyScheduleEmpty(ScheduleType.PERSONAL) // 친구 일정
        _isMoimScheduleExist.value = !isDailyScheduleEmpty(ScheduleType.MOIM) // 해당 모임 일정
    }

    // 선택 날짜에 해당되는 일정 필터링
    private fun getFilteredScheduleList(): List<CommunityCommonSchedule> {
        val scheduleList = if (isFriendCalendar) {
            _friendScheduleList.value!!.map { it.convertToCommunityModel() }
        } else {
            _moimScheduleList.value!!.map { it.convertToCommunityModel() }
        }

        return scheduleList.filter { schedule ->
            schedule.startDate <= getClickedDatePeriod().endDate &&
                    schedule.endDate >= getClickedDatePeriod().startDate
        }
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
        isShowDailyBottomSheet = !isShowDailyBottomSheet
        _prevIndex = _nowIndex
    }

    // 일정 상세 바텀 시트 닫기 - 동일한 날짜를 다시 클릭했을 경우
    fun isCloseScheduleDetailBottomSheet() = isShowDailyBottomSheet && (_prevIndex == _nowIndex)

    // 캘린더에 들어갈 한달 날짜 리스트
    fun setMonthDayList(monthDayList: List<DateTime>) {
        Log.e("CalendarViewModel", "setMonthDayList\n${monthDayList.first()}\n${monthDayList.last()}")
        _monthDateList = monthDayList
    }

    private fun isDailyScheduleEmpty(scheduleType: ScheduleType): Boolean {
        Log.d("CalendarViewModel", "isDailyScheduleEmpty(${scheduleType.name}): ${getDailySchedules(scheduleType)}")
        return getDailySchedules(scheduleType).isEmpty()
    }

    fun getDailySchedules(scheduleType: ScheduleType): ArrayList<CommunityCommonSchedule> {
        return _dailyScheduleList.filter { schedule ->
            schedule.type == scheduleType
        } as ArrayList<CommunityCommonSchedule>
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDateList[_nowIndex]
}