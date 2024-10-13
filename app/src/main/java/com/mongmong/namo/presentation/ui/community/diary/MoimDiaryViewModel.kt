package com.mongmong.namo.presentation.ui.community.diary

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.ActivityLocation
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.ParticipantInfo
import com.mongmong.namo.domain.model.ActivityPayment
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.PaymentParticipant
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.AddMoimDiaryUseCase
import com.mongmong.namo.domain.usecases.EditMoimDiaryUseCase
import com.mongmong.namo.domain.usecases.GetActivitiesUseCase
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val activityRepository: ActivityRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
    private val getActivitiesUseCase: GetActivitiesUseCase,
    private val addMoimDiaryUseCase: AddMoimDiaryUseCase,
    private val editMoimDiaryUseCase: EditMoimDiaryUseCase
) : ViewModel() {
    private val _diary = MutableLiveData<DiaryDetail>()
    val diary: LiveData<DiaryDetail> = _diary

    private val _diarySchedule = MutableLiveData<ScheduleForDiary>()
    val diarySchedule: LiveData<ScheduleForDiary> = _diarySchedule

    private val _diaryChanged = MutableLiveData<Boolean>(false)
    val diaryChanged: LiveData<Boolean> = _diaryChanged

    private val _activities = MutableLiveData<List<Activity>>(mutableListOf())
    val activities: LiveData<List<Activity>> = _activities

    private val _addDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val addDiaryResult: LiveData<DiaryBaseResponse> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val editDiaryResult: LiveData<DiaryBaseResponse> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val deleteDiaryResult: LiveData<DiaryBaseResponse> = _deleteDiaryResult

    private val _editActivityParticipantsResult = MutableLiveData<DiaryBaseResponse>()
    val editActivityParticipantsResult: LiveData<DiaryBaseResponse> = _editActivityParticipantsResult

    private val _editActivityPaymentResult = MutableLiveData<DiaryBaseResponse>()
    val editActivityPaymentResult: LiveData<DiaryBaseResponse> = _editActivityPaymentResult

    private val _isActivityAdded = MutableLiveData<Boolean>(false)
    val isActivityAdded: LiveData<Boolean> = _isActivityAdded

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialEnjoy: Int = 0

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    val isParticipantVisible = MutableLiveData<Boolean>(true)

    private var deleteDiaryImageIds = mutableListOf<Long>()
    private val deleteActivityImageIdsMap: MutableMap<Long, MutableList<Long>> = mutableMapOf()

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode


    /** 기록 */
    // 기록 일정 정보 조회
    fun getScheduleForDiary(scheduleId: Long) {
        this.scheduleId = scheduleId
        viewModelScope.launch {
            _diarySchedule.value = diaryRepository.getScheduleForDiary(scheduleId)
        }
    }

    // 기록 개별 조회
    fun getDiaryData() {
        viewModelScope.launch {
            val result = diaryRepository.getDiary(scheduleId)
            _diary.value = result
            Log.d("DiaryDetailViewModel getDiary", "$result")

            initDiaryState() // 초기 상태 저장
            deleteDiaryImageIds.clear()
            deleteActivityImageIdsMap.clear()
        }
    }

    // 빈 기록 설정
    fun setupNewDiary() {
        _diary.value = DiaryDetail(
            diaryId = 0,
            content =  "",
            diaryImages = emptyList(),
            enjoyRating = 0
        )
        initDiaryState()
        deleteDiaryImageIds.clear()
        deleteActivityImageIdsMap.clear()
    }

    // 기록 추가
    fun addDiary() {
        viewModelScope.launch {
            Log.d("MoimDiaryViewModel addDiary", "$_diary")
            _addDiaryResult.value = diary.value?.let { addMoimDiaryUseCase.execute(it, diarySchedule.value?.scheduleId!!) }
        }
    }

    // 기록 수정
    fun editDiary() {
        viewModelScope.launch {
            diary.value?.let { diary ->
                activities.value?.let { activities ->
                editMoimDiaryUseCase.execute(diary, activities, deleteActivityImageIdsMap)
            } }
        }
    }

    // 기록 삭제
    fun deleteDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.value = diary.value?.let { diaryRepository.deleteDiary(it.diaryId) }
        }
    }

    // 기록 내용 업데이트 (ui)
    fun updateContent(newContent: String) {
        _diary.value?.content = newContent
        checkForChanges()
    }

    // 기록 재미도 업데이트 (ui)
    fun updateEnjoy(count: Int) {
        _diary.value?.enjoyRating = count
        checkForChanges()
    }

    // 기록 이미지 추가 (ui)
    fun addDiaryImages(newImages: List<Uri>) {
        val currentImages = diary.value?.diaryImages ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)

        _diary.value?.diaryImages = currentImages + newImagesToAdd.map {
            DiaryImage(diaryImageId = 0L, imageUrl = it.toString(), orderNumber = 1)
        }

        _diary.value = _diary.value
        checkForChanges()
    }

    // 기록 이미지 삭제 (ui)
    fun deleteDiaryImage(image: DiaryImage) {
        if (image.diaryImageId != 0L) deleteDiaryImageIds.add(image.diaryImageId)
        _diary.value?.let {
            val updatedImages = it.diaryImages.toMutableList().apply { remove(image) }
            it.diaryImages = updatedImages
        }

        checkForChanges()
    }

    /** 활동 */
    // 활동 조회
    fun getActivitiesData() {
        viewModelScope.launch {
            val result = getActivitiesUseCase.execute(scheduleId)
            _activities.value = result
        }
    }

    // 빈 활동 추가
    fun addEmptyActivity() {
        _activities.value = _activities.value?.plus(
            Activity(
                endDate = diarySchedule.value?.date ?: "",
                activityId = 0L,
                location = ActivityLocation(),
                participants = emptyList(),
                startDate = diarySchedule.value?.date ?: "",
                title = "",
                tag = "",
                payment = ActivityPayment(participants = _diarySchedule.value?.participantInfo?.map {
                    PaymentParticipant(it.userId, it.nickname, false)
                } ?: emptyList()),
                images = emptyList()
            )
        )

        _isActivityAdded.value = true
    }

    fun activityAddedHandled() { _isActivityAdded.value = false }

    // 활동 이름 변경 (ui)
    fun updateActivityName(position: Int, title: String) {
        _activities.value?.get(position)?.title = title
    }

    // 활동 시작 시간 변경 (ui)
    fun updateActivityStartDate(position: Int, date: String) {
        _activities.value?.get(position)?.startDate = date
    }

    // 활동 종료 시간 변경 (ui)
    fun updateActivityEndDate(position: Int, date: String) {
        _activities.value?.get(position)?.endDate = date
    }

    // 활동 장소 변경 (ui)
    fun updateActivityLocation(position: Int, id: String, name: String, x: Double, y: Double) {
        _activities.value?.get(position)?.location = ActivityLocation(kakaoLocationId = id, locationName = name, longitude = x, latitude = y)
        _activities.value = _activities.value
    }

    // 활동 태그 변경 (ui)
    fun updateActivityTag(position: Int, tag: String) {
        _activities.value?.get(position)?.tag = tag
        _activities.value = _activities.value
    }

    // 활동 참가자 변경 (ui)
    fun updateActivityParticipants(position: Int, members: List<ParticipantInfo>) {
        _activities.value?.get(position)?.participants = members
        _activities.value = _activities.value
    }

    // 활동 정산 변경 (ui)
    fun updateActivityPayment(position: Int, payment: ActivityPayment) {
        _activities.value?.get(position)?.payment = payment
        Log.d("updateActivityPayment", "$payment")
        _activities.value = _activities.value
    }

    // 활동 참가자 수정
    fun editActivityParticipants(activityId: Long, participantsToAdd: List<Long>, participantsToRemove: List<Long>) {
        viewModelScope.launch {
            val result = activityRepository.editActivityParticipants(activityId, participantsToAdd, participantsToRemove)
            _editActivityParticipantsResult.value = result
        }
    }

    // 활동 정산 수정
    fun editActivityPayment(activityId: Long, payment: ActivityPayment) {
        viewModelScope.launch {
            val result = activityRepository.editActivityPayment(activityId = activityId, payment = payment)
            _editActivityPaymentResult.value = result
        }
    }

    // 활동 삭제
    fun deleteActivity(position: Int) {
        _activities.value?.get(position)?.let { activity ->
            _activities.value = _activities.value?.toMutableList()?.apply {
                removeAt(position)
            }

            if(activity.activityId != 0L) {
                viewModelScope.launch {
                    activityRepository.deleteActivity(activity.activityId)
                }
            }
        }

    }

    // 활동 이미지 추가
    fun addActivityImages(position: Int, newImages: List<Uri>) {
        val currentImages = activities.value?.get(position)?.images ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)

        _activities.value?.get(position)?.images = currentImages + newImagesToAdd.map {
            DiaryImage(diaryImageId = 0L, imageUrl = it.toString(), orderNumber = 1)
        }

        _activities.value = _activities.value
        checkForChanges()
    }

    // 활동 이미지 제거
    fun deleteActivityImage(position: Int, image: DiaryImage) {
        _activities.value?.get(position)?.let { activity ->
            if (image.diaryImageId != 0L) {
                deleteActivityImageIdsMap.getOrPut(activity.activityId) { mutableListOf() }.add(image.diaryImageId)
            }
            val updatedImages = activity.images.toMutableList().apply { remove(image) }
            activity.images = updatedImages
        }
        checkForChanges()
    }

    fun toggleIsParticipantVisible() {
        isParticipantVisible.value = !isParticipantVisible.value!!
    }

    fun setIsEditMode(isEdit: Boolean) { _isEditMode.value = isEdit }

    private fun initDiaryState() {
        initialDiaryContent = diary.value?.content ?: ""
        initialImgList = diary.value?.diaryImages ?: emptyList()
        initialEnjoy = diary.value?.enjoyRating ?: 0

        Log.d("initDiaryState", "$initialDiaryContent, $initialImgList, $initialEnjoy")
        isInitialLoadComplete = true
    }

    private fun checkForChanges() {
        if (!isInitialLoadComplete) return
        _diaryChanged.value = (
                diary.value?.content != initialDiaryContent ||
                        diary.value?.diaryImages != initialImgList ||
                        diary.value?.enjoyRating != initialEnjoy
                )
        Log.d("initDiaryState", "${_diaryChanged.value}")
    }

    companion object {
        const val PREFIX = "diary"
    }
}
