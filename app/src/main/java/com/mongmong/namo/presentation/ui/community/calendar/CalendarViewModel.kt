package com.mongmong.namo.presentation.ui.community.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.mongmong.namo.domain.model.SchedulePeriod
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor (
    private val repository: ScheduleRepository
): ViewModel() {
    // 달력에 들어가는 한달치 날짜
    private var _monthDateList: List<DateTime> = emptyList()

    // 모임 캘린더 일정
    private val _moimScheduleList = MutableLiveData<List<MoimCalendarSchedule>>()
    val moimScheduleList: LiveData<List<MoimCalendarSchedule>> = _moimScheduleList

    // 클릭한 날짜의 일정 처리
    private val _clickedDateTime = MutableLiveData<DateTime>()
    val clickedDateTime: LiveData<DateTime> = _clickedDateTime

    private var _dailyScheduleList: List<MoimCalendarSchedule> = emptyList() // 하루 일정

    private val _isMoimScheduleExist = MutableLiveData<Boolean>() // 모임 일정 정보가 있을 경우
    val isMoimScheduleExist: LiveData<Boolean> = _isMoimScheduleExist

    private val _isParticipantScheduleEmpty = MutableLiveData<Boolean>() // 친구/참석자 일정이 있을 경우
    var isParticipantScheduleEmpty: LiveData<Boolean> = _isParticipantScheduleEmpty

    // 친구 캘린더인지, 모임 캘린더인지를 구분
    var isFriendCalendar = true
    var moimSchedule = MoimScheduleDetail()

    // 임시 친구 데이터
    lateinit var friend: Friend
    var friendCategoryList: List<Category>

    init {
        friendCategoryList = listOf(
            Category(name = "일정", colorId = 4),
            Category(name = "약속", colorId = 3),
            Category(name = "피자", colorId = 8),
        )
    }

    var isShowDailyBottomSheet: Boolean = false

    private var _prevIndex = -1 // 이전에 클릭한 날짜의 index
    private var _nowIndex = 0 // 현재 클릭한 날짜의 index

    /** 모임 캘린더 일정 조회 */
    fun getMoimCalendarSchedules() {
        viewModelScope.launch {
            // 범위로 일정 목록 조회
            _moimScheduleList.value = repository.getMoimCalendarSchedules(
                moimScheduleId = moimSchedule.moimId,
                startDate = _monthDateList.first(), // 캘린더에 표시되는 첫번쨰 날짜
                endDate = _monthDateList.last() // 캘린더에 표시되는 마지막 날짜
            )
        }
    }

    private fun setDailySchedule() {
        // 선택 날짜에 해당되는 일정 필터링
        _dailyScheduleList = _moimScheduleList.value!!.filter { schedule ->
            schedule.startDate <= getClickedDatePeriod().endDate &&
                    schedule.endDate >= getClickedDatePeriod().startDate
        }
        _isParticipantScheduleEmpty.value = isDailyScheduleEmpty(false) // 친구 일정
        _isMoimScheduleExist.value = !isDailyScheduleEmpty(true) // 해당 모임 일정
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

    private fun isDailyScheduleEmpty(isMoim: Boolean): Boolean {
        Log.d("CalendarViewModel", "isDailyScheduleEmpty($isMoim): ${getDailySchedules(isMoim)}")
        return getDailySchedules(isMoim).isEmpty()
    }

    fun getDailySchedules(isMoim: Boolean): ArrayList<MoimCalendarSchedule> {
        return _dailyScheduleList.filter { schedule ->
            schedule.isCurMoim == isMoim
        } as ArrayList<MoimCalendarSchedule>
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDateList[_nowIndex]
}