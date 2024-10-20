package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.net.Uri
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.model.Participant
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import com.mongmong.namo.presentation.state.SuccessState
import com.mongmong.namo.presentation.state.SuccessType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) : ViewModel() {
    private val _moimSchedule = MutableLiveData<MoimScheduleDetail>()
    val moimSchedule: LiveData<MoimScheduleDetail> = _moimSchedule

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    var guestInvitationLink: String = "https://~"

    var isCoverImageEdit: Boolean = false

    //TODO: 참석자 수정
    var participantIdsToAdd = ArrayList<Long>(arrayListOf()) // 스케줄에 추가할 유저 ID(userId)
    var participantIdsToRemove = ArrayList<Long>(arrayListOf()) // 스케줄에서 삭제할 참가자 ID(participantId)

    // API 호출 성공 여부
    private val _successState = MutableLiveData<SuccessState>()
    var successState: LiveData<SuccessState> = _successState

    /** 모임 일정 조회 */
    private fun getMoimSchedule(moimScheduleId: Long) {
        viewModelScope.launch {
            _moimSchedule.value = repository.getMoimScheduleDetail(moimScheduleId)
            getGuestInvitationLink()
        }
    }

    /** 모임 일정 생성 */
    fun postMoimSchedule() {
        //TODO: 친구 API 연동 후 삭제
        updateMembers(listOf(Participant(userId = 4))) // 참석자 선택
        viewModelScope.launch {
            uploadImageToServer(_moimSchedule.value?.coverImg)

            _successState.value = SuccessState(
                SuccessType.ADD,
                repository.addMoimSchedule(_moimSchedule.value!!)
            )
        }
    }

    /** 모임 일정 수정 */
    //TODO: 방장만 편집 가능
    fun editMoimSchedule() {
        viewModelScope.launch {
            uploadImageToServer(_moimSchedule.value?.coverImg)

            _successState.value = SuccessState(
                SuccessType.EDIT,
                repository.editMoimSchedule(
                    _moimSchedule.value!!,
                    participantIdsToAdd,
                    participantIdsToRemove
                )
            )
        }
    }

    /** 모임 일정 삭제 */
    fun deleteMoimSchedule() {
        viewModelScope.launch {
            _successState.value = SuccessState(
                SuccessType.DELETE,
                repository.deleteMoimSchedule(_moimSchedule.value!!.moimId)
            )
        }
    }

    /** 모임 일정 프로필 변경 */
    //TODO: userId를 통해 사용자의 방장 여부를 판별 후 연동 필요
    fun editMoimScheduleProfile() {
        viewModelScope.launch {
            repository.editMoimScheduleProfile(
                _moimSchedule.value!!.moimId,
                _moimSchedule.value!!.title,
                _moimSchedule.value!!.coverImg)
        }
    }

    private fun getGuestInvitationLink() {
        viewModelScope.launch {
            guestInvitationLink = repository.getGuestInvitaionLink(_moimSchedule.value!!.moimId)
        }
    }

    private suspend fun uploadImageToServer(imageUri: String?) {
        if (imageUri.isNullOrEmpty() || !isCoverImageEdit) return
        val urlList = listOf(imageUri)

        // suspend 함수이므로 호출 시점에서 대기
        val newImageUrls = uploadImageToS3UseCase.execute(
            PREFIX, (urlList).map { Uri.parse(it) }
        )

        // 이미지 URL 업데이트
        _moimSchedule.value = _moimSchedule.value?.copy(
            coverImg = newImageUrls.first()
        )
    }

    // 모임 일정 기본 정보 세팅
    fun setMoimSchedule(moimScheduleId: Long) {
        if (moimScheduleId == 0L) { // 모임 일정 생성
            _moimSchedule.value = MoimScheduleDetail()
            return
        }
        getMoimSchedule(moimScheduleId) // 모임 일정 편집
    }

    fun updateImage(uri: Uri?) {
        isCoverImageEdit = true
        _moimSchedule.value = _moimSchedule.value?.copy(
            coverImg = uri.toString()
        )
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            //
        )
    }

    fun updateMembers(selectedMember: List<Participant>) {
        _moimSchedule.value = _moimSchedule.value!!.copy(
            participants = selectedMember
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: LocalDateTime?, endDateTime: LocalDateTime?) {
        _moimSchedule.value = _moimSchedule.value?.copy(
            period = SchedulePeriod(
                startDate = startDateTime
                    ?: _moimSchedule.value!!.period.startDate,
                endDate = endDateTime
                    ?: _moimSchedule.value!!.period.endDate
            ),
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }

    fun isCreateMode() = _moimSchedule.value!!.moimId == 0L

    fun getDateTime(): SchedulePeriod? {
        if (_moimSchedule.value != null) {
            return _moimSchedule.value!!.period
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

    companion object {
        const val PREFIX = "cover"
    }
}