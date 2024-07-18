package com.mongmong.namo.presentation.ui.home.schedule

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
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

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    private var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

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

    private fun setDefaultGroupMembers() {
        _schedule.value!!.users = _group.value!!.groupMembers
    }

    fun updateTitle(title: String) {
        _schedule.value = _schedule.value?.copy(
            name = title
        )
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _schedule.value = _schedule.value?.copy(
            placeName = placeName,
            placeX = x,
            placeY = y
        )
        Log.d("MoimScheduleViewModel", "updatePlace() - $placeName, $x, $y")
    }

    fun updateMembers(selectedMember: MoimSchduleMemberList) {
        _schedule.value = _schedule.value!!.copy(
            users = selectedMember.memberList
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: DateTime?, endDateTime: DateTime?) {
        Log.d("MoimScheduleViewModel", "setTime()\nstart: $startDateTime\nend: $endDateTime")
        _schedule.value = _schedule.value?.copy(
            startLong = startDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.startLong,
            endLong = endDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.endLong
        )
        Log.d(
            "MoimScheduleViewModel",
            "startLong: ${_schedule.value!!.startLong}\nendLong: ${_schedule.value!!.endLong}"
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }

    fun getPrevClickedPicker() = prevClickedPicker.value


    fun isCreateMode() = schedule.value!!.moimScheduleId == 0L

    fun getMembers() = schedule.value!!.users

    fun getSelectedMemberId() = schedule.value!!.users.map { it.userId }

    fun getDateTime(): Pair<DateTime, DateTime>? {
        Log.d("ScheduleViewModel", "getDateTime()")
        if (_schedule.value != null) {
            return Pair(
                PickerConverter.parseLongToDateTime(schedule.value!!.startLong),
                PickerConverter.parseLongToDateTime(schedule.value!!.endLong)
            )
        }
        return null
    }

    fun getPlace(): Pair<String, LatLng>? {
        Log.d("ScheduleViewModel", "getPlace()")
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