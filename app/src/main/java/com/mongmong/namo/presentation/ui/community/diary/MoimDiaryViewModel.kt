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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val activityRepository: ActivityRepository,
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

    private var initialDiary: DiaryDetail = DiaryDetail()
    private var initialActivities: List<Activity> = emptyList()

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    val isParticipantVisible = MutableLiveData<Boolean>(true)

    private var deleteDiaryImageIds = mutableListOf<Long>()
    private val deleteActivityImageIds: MutableMap<Long, MutableList<Long>> = mutableMapOf()

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
            deleteActivityImageIds.clear()
        }
    }

    // 빈 기록 설정
    fun setupNewDiary() {
        _diary.value = DiaryDetail()
        initDiaryState()
        deleteDiaryImageIds.clear()
        deleteActivityImageIds.clear()
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
            val updatedDiary = createUpdatedDiary()
            val updatedActivities = createUpdatedActivities()
            Log.d("MoimDiaryViewModel", "updateDiary: ${updatedDiary}\nupdatedActivities: ${updatedActivities}")
            if (updatedDiary != null || updatedActivities.isNotEmpty()) {
                // 변경된 diary나 activities가 있다면 수정 요청
                _editDiaryResult.value = editMoimDiaryUseCase.execute(
                    scheduleId= diarySchedule.value?.scheduleId!!,
                    diary = updatedDiary,
                    activities = updatedActivities,
                    deleteDiaryImageIds = deleteDiaryImageIds,
                    deleteActivityImageIds = deleteActivityImageIds
                )
            }
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

            initActivitiesState()
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
        checkForChanges()
    }

    fun activityAddedHandled() { _isActivityAdded.value = false }

    // 활동 이름 변경 (ui)
    fun updateActivityName(position: Int, title: String) {
        _activities.value?.get(position)?.title = title
        checkForChanges()
    }

    // 활동 시작 시간 변경 (ui)
    fun updateActivityStartDate(position: Int, date: String) {
        _activities.value?.get(position)?.startDate = date
        checkForChanges()
    }

    // 활동 종료 시간 변경 (ui)
    fun updateActivityEndDate(position: Int, date: String) {
        _activities.value?.get(position)?.endDate = date
        checkForChanges()
    }

    // 활동 장소 변경 (ui)
    fun updateActivityLocation(position: Int, id: String, name: String, x: Double, y: Double) {
        _activities.value?.get(position)?.location = ActivityLocation(kakaoLocationId = id, locationName = name, longitude = x, latitude = y)
        _activities.value = _activities.value
        checkForChanges()
    }

    // 활동 태그 변경 (ui)
    fun updateActivityTag(position: Int, tag: String) {
        _activities.value?.get(position)?.tag = tag
        _activities.value = _activities.value
        checkForChanges()
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
                deleteActivityImageIds.getOrPut(activity.activityId) { mutableListOf() }.add(image.diaryImageId)
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
        // 일기장 초기 상태 저장
        initialDiary = _diary.value?.copy(
            diaryImages = _diary.value?.diaryImages?.map { it.copy() } ?: emptyList()
        ) ?: DiaryDetail()
    }

    private fun initActivitiesState() {
        // 활동 초기 상태 저장
        initialActivities = _activities.value?.map { activity ->
            activity.copy(
                participants = activity.participants.map { it.copy() },
                images = activity.images.map { it.copy() },
                location = activity.location.copy(),
                payment = activity.payment.copy(participants = activity.payment.participants.map { it.copy() })
            )
        } ?: emptyList()
    }

    // 변경된 항목이 있으면 diary, 없으면 null
    private fun createUpdatedDiary(): DiaryDetail? {
        val currentDiary = _diary.value ?: return null

        // 변경된 필드를 가진 diary만 반환
        return if (checkDiaryChanged()) {
            currentDiary.copy(
                diaryImages = currentDiary.diaryImages?.filter { it.diaryImageId == 0L } ?: emptyList()
            )
        } else null

    }

    // 변경된 항목만 가진 활동 리스트를 반환
    private fun createUpdatedActivities(): List<Activity> {
        val currentActivities = _activities.value ?: return emptyList()

        return currentActivities.filter { currentActivity ->
            val initialActivity = initialActivities.find { it.activityId == currentActivity.activityId }

            currentActivity.activityId == 0L || initialActivity == null || isActivityChanged(currentActivity, initialActivity)
        }
    }

    private fun checkForChanges() {
        val diaryChanged = checkDiaryChanged()
        val activitiesChanged = checkActivitiesChanged()

        // 변경 사항이 하나라도 있으면 true로 설정
        _diaryChanged.value = diaryChanged || activitiesChanged
    }

    private fun checkDiaryChanged(): Boolean {
        val currentDiary = _diary.value ?: return false

        // 내용 변경 체크
        if (currentDiary.content != initialDiary.content) return true
        // 재미도 변경 체크
        if (currentDiary.enjoyRating != initialDiary.enjoyRating) return true
        // 이미지 변경 체크
        if (currentDiary.diaryImages.size != initialDiary.diaryImages.size ||
            currentDiary.diaryImages.any { it.imageUrl != initialDiary.diaryImages.find { img -> img.imageUrl == it.imageUrl }?.imageUrl }
        ) return true

        return false
    }

    private fun checkActivitiesChanged(): Boolean {
        val currentActivities = _activities.value ?: return false
        // 추가된 활동 존재 여부 체크
        if (currentActivities.any { it.activityId == 0L }) return true

        // 각각의 활동에 대한 변경 사항 체크
        return currentActivities.any { currentActivity ->
            val initialActivity = initialActivities.find { it.activityId == currentActivity.activityId }
            initialActivity == null || isActivityChanged(currentActivity, initialActivity)
        }
    }

    private fun isActivityChanged(currentActivity: Activity, initialActivity: Activity): Boolean {
        // 제목 변경 체크
        Log.d("isActivityChanged", "${currentActivity.title}")
        if (currentActivity.title != initialActivity.title) return true
        // 시작일 변경 체크
        if (currentActivity.startDate != initialActivity.startDate) return true
        // 종료일 변경 체크
        if (currentActivity.endDate != initialActivity.endDate) return true
        // 장소 변경 체크
        if (currentActivity.location != initialActivity.location) return true
        // 이미지 변경 체크
        if (currentActivity.images.size != initialActivity.images.size ||
            currentActivity.images.any { it.imageUrl != initialActivity.images.find { img -> img.imageUrl == it.imageUrl }?.imageUrl }
        ) return true

        return false
    }
}
