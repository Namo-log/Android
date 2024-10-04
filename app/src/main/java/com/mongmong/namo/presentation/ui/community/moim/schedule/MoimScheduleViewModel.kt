package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kakao.vectormap.LatLng
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.domain.model.group.MoimSchduleMemberList
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.utils.PickerConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    private val _moimSchedule = MutableLiveData<Moim>()
    val moimSchedule: LiveData<Moim> = _moimSchedule

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    private val _monthDayList = MutableLiveData<List<DateTime>>()

    val moimTitle: MutableLiveData<String> = MutableLiveData()

    //TODO: 임시 데이터
    private val _participantList = MutableLiveData<List<GroupMember>>()
    val participantList: LiveData<List<GroupMember>> = _participantList

    // 클릭한 날짜 처리
    private lateinit var _dailyScheduleList: List<MoimScheduleBody>

    private val _isShow = MutableLiveData(false)
    var isShow: LiveData<Boolean> = _isShow

    private var _prevIndex = -1 // 클릭한 날짜의 index
    private var _nowIndex = 0 // 클릭한 날짜의 index

    private lateinit var _clickedDatePair: Pair<Long, Long> // 클릭한 날짜의 시작, 종료 시간

    private val _isDailyScheduleEmptyPair = MutableLiveData<Pair<Boolean, Boolean>>()
    var isDailyScheduleEmptyPair: LiveData<Pair<Boolean, Boolean>> = _isDailyScheduleEmptyPair

    init {
        _participantList.value = listOf(
            GroupMember(0, "코코아", 1),
            GroupMember(0, "짱구", 4),
            GroupMember(0, "뚜뚜", 8),
        )
    }

    /** 모임 일정 생성 */
    fun postMoimSchedule() {

    }

    /** 모임 일정 수정 */
    fun editMoimSchedule() {

    }

    /** 모임 일정 삭제 */
    fun deleteMoimSchedule() {

    }

    /** 모임 일정 정보 세팅 */
    fun setMoimSchedule(moim: Moim) {
        _moimSchedule.value = moim
        moimTitle.value = moim.title
        if (moim.placeName.isBlank()) {
            _moimSchedule.value?.placeName = "없음"
        }
    }

    private fun setDailySchedule() {
        // 선택 날짜에 해당되는 일정 필터링
//        _dailyScheduleList = _groupScheduleList.value!!.filter { schedule ->
//            schedule.startLong <= _clickedDatePair.second &&
//                    schedule.endLong >= _clickedDatePair.first
//        }
//        _isDailyScheduleEmptyPair.value = Pair(
//            isDailyScheduleEmpty(false), // 개인 일정
//            isDailyScheduleEmpty(true) // 모임 일정
//        )
    }

    // 캘린더의 날짜 클릭
    fun clickDate(index: Int) {
        _nowIndex = index
        // 클릭한 날짜의 시작, 종료 시간 저장
        _clickedDatePair = Pair(
            (getClickedDate().withTimeAtStartOfDay().millis) / 1000, // 날짜 시작일
            (getClickedDate().plusDays(1).withTimeAtStartOfDay().millis - 1) / 1000, // 날짜 종료일
        )
        setDailySchedule()
    }

    fun updateIsShow() {
        _isShow.value = !_isShow.value!!
        _prevIndex = _nowIndex
    }

    // 일정 상세 바텀 시트 닫기 - 동일한 날짜를 다시 클릭했을 경우
    fun isCloseScheduleDetailBottomSheet() = _isShow.value == true && (_prevIndex == _nowIndex)

    // 캘린더에 들어갈 한달 날짜 리스트
    fun setMonthDayList(monthDayList: List<DateTime>) {
        _monthDayList.value = monthDayList
    }

    fun filterMonthSchedule() {
        val monthStart = _monthDayList.value!![0].withTimeAtStartOfDay().millis / 1000
        val monthEnd = _monthDayList.value!![41].plusDays(1).withTimeAtStartOfDay().millis / 1000
//        _monthScheduleList.value = _groupScheduleList.value?.filter { schedule ->
//            schedule.startLong <= monthEnd && schedule.endLong >= monthStart
//        }
    }

    fun setIsShow(bool: Boolean) {
        _isShow.value = bool
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            placeName = placeName,
//            placeX = x,
//            placeY = y
        )
    }

    fun updateMembers(selectedMember: MoimSchduleMemberList) {
        _moimSchedule.value = _moimSchedule.value!!.copy(
            members = selectedMember.memberList
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: LocalDateTime?, endDateTime: LocalDateTime?) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            startDate = startDateTime
                ?: _moimSchedule.value!!.startDate,
//            endDate = endDateTime
//                ?: _moimSchedule.value!!.endDate,
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }


    fun isCreateMode() = _moimSchedule.value!!.moimId == 0L

    private fun isDailyScheduleEmpty(isMoim: Boolean): Boolean {
        Log.d("ScheduleViewModel", "isDailyScheduleEmpty($isMoim): ${getDailySchedules(isMoim)}")
        return getDailySchedules(isMoim).isEmpty()
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDayList.value!![_nowIndex]

    fun getDailySchedules(isMoim: Boolean): ArrayList<MoimScheduleBody> {
        return _dailyScheduleList.filter { schedule ->
            schedule.curMoimSchedule == isMoim
        } as ArrayList<MoimScheduleBody>
    }

    fun getSelectedMemberId() = _moimSchedule.value!!.members.map { it.userId }

    fun getDateTime(): SchedulePeriod? {
        if (_moimSchedule.value != null) {
            return SchedulePeriod(
                moimSchedule.value!!.startDate,
                moimSchedule.value!!.startDate,
            )
        }
        return null
    }

    fun getPlace(): Pair<String, LatLng>? {
//        if (_moimSchedule.value != null) {
//            if (_moimSchedule.value!!.placeX == 0.0 && _schedule.value!!.placeY == 0.0) return null
//            return Pair(
//                _moimSchedule.value!!.placeName,
//                LatLng.from(_moimSchedule.value!!.placeY, _moimSchedule.value!!.placeX)
//            )
//        }
        return null
    }
}