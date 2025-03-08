package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.net.Uri
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.model.SchedulePeriod
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.domain.usecases.image.UploadImageToS3UseCase
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import com.mongmong.namo.presentation.enums.SuccessState
import com.mongmong.namo.presentation.enums.SuccessType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.joda.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) : ViewModel() {
    var moimScheduleId: Long = -1

    private val _moimSchedule = MutableLiveData<MoimScheduleDetail>()
    val moimSchedule: LiveData<MoimScheduleDetail> = _moimSchedule

    private val _isCurrentUserOwner = MutableLiveData<Boolean>(false) // 로그인 한 유저가 모임의 방장인지
    val isCurrentUserOwner: LiveData<Boolean> = _isCurrentUserOwner

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    var guestInvitationLink: String = "https://~"

    var isCoverImageEdit: Boolean = false

    var participantIdsToAdd = ArrayList<Long>(arrayListOf()) // 스케줄에 추가할 유저 ID(userId)
    var participantIdsToRemove = ArrayList<Long>(arrayListOf()) // 스케줄에서 삭제할 참가자 ID(participantId)

    var createdMoimId: Long = -1

    // API 호출 성공 여부
    private val _successState = MutableLiveData<SuccessState>()
    var successState: LiveData<SuccessState> = _successState

    /** 모임 일정 상세 조회 */
    fun getMoimScheduleDetailInfo() {
        viewModelScope.launch {
            _moimSchedule.value = repository.getMoimScheduleDetail(moimScheduleId)
            getGuestInvitationLink()
            checkIsCurrentUserOwner() // 현재 로그인 한 유저가 모임의 방장인지를 확인
        }
    }

    /** 모임 일정 생성 */
    fun postMoimSchedule() {
        viewModelScope.launch {
            uploadImageToServer(_moimSchedule.value?.coverImg)
            createdMoimId = repository.addMoimSchedule(_moimSchedule.value!!)

            _successState.value = SuccessState(
                SuccessType.ADD,
                createdMoimId >= 0
            )
        }
    }

    /** 모임 일정 수정 */
    private fun editMoimSchedule() { // 방장만 편집 가능
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
    private fun editMoimScheduleProfile() {
        viewModelScope.launch {
            uploadImageToServer(_moimSchedule.value?.coverImg)

            _successState.value = SuccessState(
                SuccessType.EDIT,
                repository.editMoimScheduleProfile(
                    _moimSchedule.value!!.moimId,
                    _moimSchedule.value!!.title,
                    _moimSchedule.value!!.coverImg)
            )
        }
    }

    private fun getGuestInvitationLink() {
        viewModelScope.launch {
            guestInvitationLink = repository.getGuestInvitationLink(_moimSchedule.value!!.moimId)
        }
    }

    private fun checkIsCurrentUserOwner() {
        val myInfo = _moimSchedule.value!!.participants.find { it.userId == getMyUserId() }
        Log.d("MoimScheduleVM", "myUserId: ${getMyUserId()}, isOwner: ${myInfo?.isOwner}")
        _isCurrentUserOwner.value = myInfo?.isOwner
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
    fun setMoimSchedule() {
        if (moimScheduleId == 0L) { // 모임 일정 생성
            _moimSchedule.value = MoimScheduleDetail()
            return
        }
        getMoimScheduleDetailInfo() // 모임 일정 편집
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

    fun onClickEditButton() {
        if (_isCurrentUserOwner.value == true) { // 모임 일정 편집
            editMoimSchedule()
        } else { // 모임 일정 프로필 수정
            editMoimScheduleProfile()
        }
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

    // 저장된 userId 가져오기
    private fun getMyUserId(): Long = runBlocking {
        dsManager.getUserId().first() ?: 0L
    }

    // 모임 참석자들의 userId
    fun getParticipantUserIdList(): List<Long> {
        return _moimSchedule.value!!.participants.filter {
            it.userId != getMyUserId() // 내 id 제외
        }.map { it.userId }
    }

    companion object {
        const val PREFIX = "cover"
    }
}