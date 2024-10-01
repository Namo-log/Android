package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.model.group.MoimSchduleMemberList
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.utils.PickerConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    private val _moimSchedule = MutableLiveData<MoimScheduleDetail>()
    val moimSchedule: LiveData<MoimScheduleDetail> = _moimSchedule

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    val moimTitle: MutableLiveData<String> = MutableLiveData()

    /** 모임 일정 조회 */
    private fun getMoimSchedule(moimScheduleId: Long) {
        viewModelScope.launch {
            _moimSchedule.value = repository.getMoimScheduleDetail(moimScheduleId)
            moimTitle.value = _moimSchedule.value!!.title
        }
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
    fun setMoimSchedule(moimScheduleId: Long) {
        if (moimScheduleId == 0L) { // 모임 일정 생성
            _moimSchedule.value = MoimScheduleDetail()
            return
        }
        getMoimSchedule(moimScheduleId) // 모임 일정 편집
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            //
        )
    }

    fun updateMembers(selectedMember: MoimSchduleMemberList) {
        _moimSchedule.value = _moimSchedule.value!!.copy(
//            members = selectedMember.memberList
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: DateTime?, endDateTime: DateTime?) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            startDate = startDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _moimSchedule.value!!.startDate,
            endDate = endDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _moimSchedule.value!!.endDate
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }


    fun isCreateMode() = _moimSchedule.value!!.moimId == 0L

//    fun getSelectedMemberId() = _moimSchedule.value!!.members.map { it.userId }

    fun getDateTime(): Pair<DateTime, DateTime>? {
        if (_moimSchedule.value != null) {
            return Pair(
                PickerConverter.parseLongToDateTime(_moimSchedule.value!!.startDate),
                PickerConverter.parseLongToDateTime(_moimSchedule.value!!.startDate)
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