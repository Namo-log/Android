package com.mongmong.namo.presentation.ui.group.schedule

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.BaseMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.MoimSchduleMemberList
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.utils.PickerConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    private val _schedule = MutableLiveData<MoimScheduleBody>()
    val schedule: LiveData<MoimScheduleBody> = _schedule

    private val _group = MutableLiveData<Group>()
    val group: LiveData<Group> = _group

    private val _groupScheduleList = MutableLiveData<List<MoimScheduleBody>>(emptyList())
    val groupScheduleList: LiveData<List<MoimScheduleBody>?> = _groupScheduleList

    private val _monthScheduleList = MutableLiveData<List<MoimScheduleBody>>(emptyList())
    val monthScheduleList: LiveData<List<MoimScheduleBody>?> = _monthScheduleList

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    private val _monthDayList = MutableLiveData<List<DateTime>>()

    // 클릭한 날짜 처리
    private val _dailyScheduleList = MutableLiveData<List<MoimScheduleBody>>(emptyList())
    val dailyScheduleList: LiveData<List<MoimScheduleBody>> = _dailyScheduleList

    private val _isShow = MutableLiveData(false)
    var isShow: LiveData<Boolean> = _isShow

    private var _prevIndex = -1 // 클릭한 날짜의 index
    private var _nowIndex = 0 // 클릭한 날짜의 index

    private val _clickedDatePair = MutableLiveData<Pair<Long, Long>>() // 클릭한 날짜의 시작, 종료 시간

    private val _isDailyScheduleEmptyPair = MutableLiveData<Pair<Boolean, Boolean>>()
    var isDailyScheduleEmptyPair: LiveData<Pair<Boolean, Boolean>> = _isDailyScheduleEmptyPair

    /** 그룹의 모든 일정 조회 */
    fun getGroupAllSchedules(groupId: Long) {
        viewModelScope.launch {
            Log.d("MoimScheduleViewModel", "getGroupAllSchedules")
            _groupScheduleList.value = repository.getGroupAllSchedules(groupId)
        }
    }

    /** 모임 일정 생성 */
    fun postMoimSchedule() {
        val addRequest = AddMoimScheduleRequestBody(groupId = _group.value!!.groupId)
        (_schedule.value!!.convertMoimScheduleToBaseRequest()).copyPropertiesTo(addRequest)
        viewModelScope.launch {
            repository.addMoimSchedule(addRequest)
        }
    }

    /** 모임 일정 수정 */
    fun editMoimSchedule() {
        val editRequest = EditMoimScheduleRequestBody(moimScheduleId = _schedule.value!!.moimScheduleId)
        (_schedule.value!!.convertMoimScheduleToBaseRequest()).copyPropertiesTo(editRequest)
        viewModelScope.launch {
            repository.editMoimSchedule(editRequest)
        }
    }

    /** 모임 일정 삭제 */
    fun deleteMoimSchedule() {
        viewModelScope.launch {
            repository.deleteMoimSchedule(_schedule.value!!.moimScheduleId)
        }
    }

    /** 모임 일정 정보 세팅 */
    fun setSchedule(schedule: MoimScheduleBody) {
        _schedule.value = schedule
        if (isCreateMode()) {
            setDefaultGroupMembers()
            _schedule.value?.groupId = _group.value!!.groupId
        }
        if (schedule.placeName.isBlank()) {
            _schedule.value?.placeName = "없음"
        }
    }

    fun setGroup(group: Group) {
        _group.value = group
    }

    private fun setDailySchedule() {
        // 선택 날짜에 해당되는 일정 필터링
        _dailyScheduleList.value = _groupScheduleList.value!!.filter { schedule ->
            schedule.startLong <= _clickedDatePair.value!!.second &&
                    schedule.endLong >= _clickedDatePair.value!!.first
        }
        _isDailyScheduleEmptyPair.value = Pair(
            isDailyScheduleEmpty(false), // 개인 일정
            isDailyScheduleEmpty(true) // 모임 일정
        )
    }

    // 캘린더의 날짜 클릭
    fun clickDate(index: Int) {
        _nowIndex = index
        // 클릭한 날짜의 시작, 종료 시간 저장
        _clickedDatePair.value = Pair(
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
        _monthScheduleList.value = _groupScheduleList.value?.filter { schedule ->
            schedule.startLong <= monthEnd && schedule.endLong >= monthStart
        }
    }

    fun setIsShow(bool: Boolean) {
        _isShow.value = bool
    }

    private fun setDefaultGroupMembers() {
        _schedule.value!!.members = _group.value!!.groupMembers
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _schedule.value = _schedule.value?.copy(
            placeName = placeName,
            placeX = x,
            placeY = y
        )
    }

    fun updateMembers(selectedMember: MoimSchduleMemberList) {
        _schedule.value = _schedule.value!!.copy(
            members = selectedMember.memberList
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: DateTime?, endDateTime: DateTime?) {
        _schedule.value = _schedule.value?.copy(
            startLong = startDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.startLong,
            endLong = endDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.endLong
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }


    fun isCreateMode() = schedule.value!!.moimScheduleId == 0L

    private fun isDailyScheduleEmpty(isMoim: Boolean): Boolean {
        Log.d("ScheduleViewModel", "isDailyScheduleEmpty($isMoim): ${getDailySchedules(isMoim)}")
        return getDailySchedules(isMoim).isEmpty()
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDayList.value!![_nowIndex]

    fun getDailySchedules(isMoim: Boolean): ArrayList<MoimScheduleBody> {
        return _dailyScheduleList.value!!.filter { schedule ->
            schedule.curMoimSchedule == isMoim
        } as ArrayList<MoimScheduleBody>
    }

    fun getSelectedMemberId() = schedule.value!!.members.map { it.userId }

    fun getDateTime(): Pair<DateTime, DateTime>? {
        if (_schedule.value != null) {
            return Pair(
                PickerConverter.parseLongToDateTime(schedule.value!!.startLong),
                PickerConverter.parseLongToDateTime(schedule.value!!.endLong)
            )
        }
        return null
    }

    fun getPlace(): Pair<String, LatLng>? {
        if (_schedule.value != null) {
            if (_schedule.value!!.placeX == 0.0 && _schedule.value!!.placeY == 0.0) return null
            return Pair(
                schedule.value!!.placeName,
                LatLng.from(schedule.value!!.placeY, schedule.value!!.placeX)
            )
        }
        return null
    }

    fun <T : BaseMoimScheduleRequestBody> BaseMoimScheduleRequestBody.copyPropertiesTo(target: T): T {
        this::class.memberProperties.forEach { property ->
            if (property is KMutableProperty1<*, *>) {
                val targetProperty = target::class.memberProperties.find { it.name == property.name } as? KMutableProperty1<T, Any?>
                targetProperty?.let {
                    val value = (property as KMutableProperty1<BaseMoimScheduleRequestBody, Any?>).get(this)
                    it.set(target, value)
                }
            }
        }
        return target
    }
}