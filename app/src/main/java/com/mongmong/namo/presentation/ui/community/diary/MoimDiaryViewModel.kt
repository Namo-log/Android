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
import com.mongmong.namo.domain.model.PaymentParticipant
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.GetActivitiesUseCase
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import com.mongmong.namo.presentation.ui.diary.PersonalDiaryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
    private val getActivitiesUseCase: GetActivitiesUseCase
) : ViewModel() {
    private val _diary = MutableLiveData<DiaryDetail>()
    val diary: LiveData<DiaryDetail> = _diary

    private val _diarySchedule = MutableLiveData<ScheduleForDiary>()
    val diarySchedule: LiveData<ScheduleForDiary> = _diarySchedule

    private val _diaryChanged = MutableLiveData<Boolean>(false)
    val diaryChanged: LiveData<Boolean> = _diaryChanged

    private val _activities = MutableLiveData<List<Activity>>(mutableListOf())
    val activities: LiveData<List<Activity>> = _activities

    private val _addDiaryResult = MutableLiveData<Boolean>()
    val addDiaryResult: LiveData<Boolean> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<Boolean>()
    val editDiaryResult: LiveData<Boolean> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<Boolean>()
    val deleteDiaryResult: LiveData<Boolean> = _deleteDiaryResult

    private val _isActivityAdded = MutableLiveData<Boolean>(false)
    val isActivityAdded: LiveData<Boolean> = _isActivityAdded

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialEnjoy: Int = 0

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    private var deleteImageIds = mutableListOf<Long>()

    private val _patchActivitiesComplete = MutableLiveData<Boolean>()
    val patchActivitiesComplete: LiveData<Boolean> = _patchActivitiesComplete

    private val _deleteDiaryComplete = MutableLiveData<Boolean>()
    val deleteDiaryComplete: LiveData<Boolean> = _deleteDiaryComplete

    val isParticipantVisible = MutableLiveData<Boolean>(true)
    private val deleteActivityIds = mutableListOf<Long>()  // 삭제할 항목 저장

    private val deleteImageIdsMap: MutableMap<Long, MutableList<Long>> = mutableMapOf()

    private val _isEditMode = MutableLiveData<Boolean>(false)
    val isEditMode: LiveData<Boolean> = _isEditMode



    /** 기록 */
    // 기록 일정 정보 조회
    fun getScheduleForDiary(scheduleId: Long) {
        this.scheduleId = scheduleId
        viewModelScope.launch {
            _diarySchedule.value = repository.getScheduleForDiary(scheduleId)
        }
    }

    // 기록 개별 조회
    fun getDiaryData() {
        viewModelScope.launch {
            val result = repository.getDiary(scheduleId)
            _diary.value = result
            Log.d("DiaryDetailViewModel getDiary", "$result")

            initDiaryState() // 초기 상태 저장
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
    }

    // 기록 추가
    fun addDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            val newImageUrls = uploadImageToS3UseCase.execute(
                PersonalDiaryViewModel.PREFIX, (diary.value?.diaryImages ?: emptyList()).map { Uri.parse(it.imageUrl) }
            )

            _addDiaryResult.value =
                repository.addDiary(
                    content = diary.value?.content ?: "",
                    enjoyRating = diary.value?.enjoyRating ?: 3,
                    images = newImageUrls,
                    scheduleId = scheduleId
                )
            deleteImageIds.clear()
        }
    }

    // 기록 수정
    fun editDiary() {
        viewModelScope.launch {
            diary.value?.diaryId?.let { diaryId ->
                // 새로운 이미지 S3에 업로드
                val newImageUrls = uploadImageToS3UseCase.execute(
                    PersonalDiaryViewModel.PREFIX,
                    diary.value?.diaryImages?.filter { it.diaryImageId == 0L }
                        ?.map { Uri.parse(it.imageUrl) }
                        ?: emptyList()
                )

                // 서버에 데이터 전송
                _editDiaryResult.value =
                    repository.editDiary(
                        content = diary.value?.content ?: "",
                        enjoyRating = diary.value?.enjoyRating ?: 3,
                        images = (
                                diary.value?.diaryImages?.filter { it.diaryImageId != 0L }
                                    ?.map { it.imageUrl }
                                    ?: emptyList()
                                ) + newImageUrls,
                        diaryId = diaryId,
                        deleteImageIds = deleteImageIds
                    )

                // 삭제할 이미지 ID 리스트 초기화
                deleteImageIds.clear()
            }
        }
    }


    // 기록 삭제
    fun deleteDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.value = diary.value?.let { repository.deleteDiary(it.diaryId) }
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

    fun activityAddedHandled() {
        _isActivityAdded.value = false
    }

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


    fun deleteActivity(position: Int) {
        _activities.value?.get(position)?.let {
            if (it.activityId != 0L) { deleteActivityIds.add(it.activityId) }

            _activities.value = _activities.value?.toMutableList()?.apply {
                removeAt(position)
            }
        }
    }


    fun addActivityImages(position: Int, newImages: List<Uri>) {
        val currentImages = activities.value?.get(position)?.images ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)

        _activities.value?.get(position)?.images = currentImages + newImagesToAdd.map {
            DiaryImage(diaryImageId = 0L, imageUrl = it.toString(), orderNumber = 1)
        }

        _activities.value = _activities.value
        checkForChanges()
    }

/*    fun deleteActivityImage(position: Int, diaryImage: DiaryImage) {
        val activity = _activities.value?.get(position) ?: return

        if (activity.moimActivityId != 0L && diaryImage.diaryImageId != 0L) {
            // deleteImageIdsMap에 activityId를 키로 사용하여 imageId를 추가
            deleteImageIdsMap.getOrPut(activity.moimActivityId) { mutableListOf() }.add(diaryImage.diaryImageId)
        }

        activity.images?.remove(diaryImage)
        _activities.value = _activities.value
    }

    fun patchMoimActivities() {
        viewModelScope.launch {
            val activities = _activities.value ?: return@launch
            activities.forEach { activity ->
                if (activity.activityId > 0L) editMoimActivity(activity)
                else addMoimActivity(activity)
            }
            deleteItems.filter { it != 0L }.forEach { activityId ->
                deleteMoimActivity(activityId)
            }
            _patchActivitiesComplete.value = true
        }
    }*/

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
