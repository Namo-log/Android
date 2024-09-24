package com.mongmong.namo.presentation.ui.group.diary

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.model.group.MoimActivity
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import com.mongmong.namo.presentation.ui.diary.PersonalDiaryViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) : ViewModel() {
    private val _diary = MutableLiveData<DiaryDetail>()
    val diary: LiveData<DiaryDetail> = _diary

    private val _diarySchedule = MutableLiveData<ScheduleForDiary>()
    val diarySchedule: LiveData<ScheduleForDiary> = _diarySchedule

    private val _diaryChanged = MutableLiveData<Boolean>(false)
    val diaryChanged: LiveData<Boolean> = _diaryChanged

    private val _addDiaryResult = MutableLiveData<Boolean>()
    val addDiaryResult: LiveData<Boolean> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<Boolean>()
    val editDiaryResult: LiveData<Boolean> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<Boolean>()
    val deleteDiaryResult: LiveData<Boolean> = _deleteDiaryResult

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialEnjoy: Int = 0

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    private var deleteImageIds = mutableListOf<Long>()

    private val _activities = MutableLiveData<MutableList<MoimActivity>>(mutableListOf())
    val activities: LiveData<MutableList<MoimActivity>> = _activities

    private val _patchActivitiesComplete = MutableLiveData<Boolean>()
    val patchActivitiesComplete: LiveData<Boolean> = _patchActivitiesComplete

    private val _deleteDiaryComplete = MutableLiveData<Boolean>()
    val deleteDiaryComplete: LiveData<Boolean> = _deleteDiaryComplete

    val isEdit = MutableLiveData<Boolean>(false)
    val isParticipantVisible = MutableLiveData<Boolean>(false)
    private val deleteItems = mutableListOf<Long>()  // 삭제할 항목 저장

    private val deleteImageIdsMap: MutableMap<Long, MutableList<Long>> = mutableMapOf()

    // 추가한 activity 임시 id
    private var tempIdCounter = -1L


    fun getScheduleForDiary(scheduleId: Long) {
        this.scheduleId = scheduleId
        viewModelScope.launch {
            _diarySchedule.postValue(repository.getScheduleForDiary(scheduleId))
        }
    }

    // 개인 기록 개별 조회
    fun getDiary() {
        viewModelScope.launch {
            val result = repository.getDiary(scheduleId)
            _diary.postValue(result)
            Log.d("DiaryDetailViewModel getPersonalDiary", "$result")

            initDiaryState() // 초기 상태 저장
            isInitialLoadComplete = true
        }
    }

    // 개인 기록 추가시 데이터 초기화
    fun setNewDiary() {
        _diary.value = DiaryDetail(
            diaryId = 0,
            content =  "",
            diaryImages = emptyList(),
            enjoyRating = 0
        )

        initDiaryState()
        isInitialLoadComplete = true
    }


    // 개인 기록 추가
    fun addDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            val newImageUrls = uploadImageToS3UseCase.execute(
                PersonalDiaryViewModel.PREFIX, (diary.value?.diaryImages ?: emptyList()).map { Uri.parse(it.imageUrl) }
            )

            _addDiaryResult.postValue(
                repository.addDiary(
                    content = diary.value?.content ?: "",
                    enjoyRating = diary.value?.enjoyRating ?: 3,
                    images = newImageUrls,
                    scheduleId = scheduleId
                )
            )
            deleteImageIds.clear()
        }
    }

    // 개인 기록 수정
    fun editDiary() {
        viewModelScope.launch {
            diary.value?.diaryId?.let { diaryId ->
                // 새로운 이미지 S3에 업로드
                val newImageUrls = uploadImageToS3UseCase.execute(
                    PersonalDiaryViewModel.PREFIX,
                    diary.value?.diaryImages
                        ?.filter { it.diaryImageId == 0L }
                        ?.map { Uri.parse(it.imageUrl) }
                        ?: emptyList()
                )

                // 서버에 데이터 전송
                _editDiaryResult.postValue(
                    repository.editDiary(
                        content = diary.value?.content ?: "",
                        enjoyRating = diary.value?.enjoyRating ?: 3,
                        images = (
                                diary.value?.diaryImages
                                    ?.filter { it.diaryImageId != 0L }
                                    ?.map { it.imageUrl }
                                    ?: emptyList()
                                ) + newImageUrls,
                        diaryId = diaryId,
                        deleteImageIds = deleteImageIds
                    )
                )

                // 삭제할 이미지 ID 리스트 초기화
                deleteImageIds.clear()
            }
        }
    }


    // 개인 기록 삭제
    fun deleteDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.postValue(diary.value?.let { repository.deleteDiary(it.diaryId) })
        }
    }

    fun updateContent(newContent: String) {
        _diary.value?.content = newContent
    }

    fun updateEnjoy(count: Int) {
        _diary.value?.enjoyRating = count
    }

    // 이미지 업데이트 시
    fun updateDiaryImages(newImages: List<DiaryImage>) {
        _diary.value?.let {
            val updatedImages = it.diaryImages.toMutableList().apply { addAll(newImages) }
            it.diaryImages = updatedImages
        }
    }

    // 이미지 삭제 시
    fun deleteDiaryImage(image: DiaryImage) {
        _diary.value?.let {
            val updatedImages = it.diaryImages.toMutableList().apply { remove(image) }
            it.diaryImages = updatedImages
        }
    }

    fun addActivity(activity: MoimActivity) {
        activity.moimActivityId = tempIdCounter--
        _activities.value?.add(activity)
        _activities.postValue(_activities.value)
    }

    fun updateActivityName(position: Int, name: String) {
        _activities.value?.get(position)?.name = name
    }

    fun updateActivityPay(position: Int, pay: Long) {
        _activities.value?.get(position)?.pay = pay
    }

    fun updateActivityMembers(position: Int, members: List<Long>) {
        _activities.value?.get(position)?.members = members
    }


    fun deleteActivity(activityId: Long) {
        if (activityId > 0) deleteItems.add(activityId)
        _activities.value = _activities.value?.filterNot { it.moimActivityId == activityId }?.toMutableList()
        deleteImageIdsMap.remove(activityId)
        _activities.postValue(_activities.value)
    }


    fun updateActivityImages(position: Int, images: List<DiaryImage>) {
        _activities.value?.get(position)?.images?.addAll(images)
        _activities.postValue(_activities.value)
    }

    fun deleteActivityImage(position: Int, diaryImage: DiaryImage) {
        val activity = _activities.value?.get(position) ?: return

        if (activity.moimActivityId != 0L && diaryImage.diaryImageId != 0L) {
            // deleteImageIdsMap에 activityId를 키로 사용하여 imageId를 추가
            deleteImageIdsMap.getOrPut(activity.moimActivityId) { mutableListOf() }.add(diaryImage.diaryImageId)
        }

        activity.images?.remove(diaryImage)
        _activities.postValue(_activities.value)
    }

    fun patchMoimActivities() {
        viewModelScope.launch {
            val activities = _activities.value ?: return@launch
            activities.forEach { activity ->
                if (activity.moimActivityId > 0L) editMoimActivity(activity)
                else addMoimActivity(activity)
            }
            deleteItems.filter { it != 0L }.forEach { activityId ->
                deleteMoimActivity(activityId)
            }
            _patchActivitiesComplete.postValue(true)
        }
    }

    private suspend fun addMoimActivity(activity: MoimActivity) {
        //val members = activity.members.ifEmpty { _moimDiary.value?.users?.map { it.userId } }
        //repository.addMoimActivity(scheduleId, activity.name, activity.pay, members ?: emptyList(), activity.getImageUrls())
    }

    private suspend fun editMoimActivity(activity: MoimActivity) {
        //val members = activity.members.ifEmpty { _moimDiary.value?.users?.map { it.userId } }
        val createImages = activity.images?.filter { it.diaryImageId == 0L }?.map { it.imageUrl }
        val deleteImageIds = deleteImageIdsMap[activity.moimActivityId]
        //repository.editMoimActivity(activity.moimActivityId, deleteImageIds,activity.name, activity.pay, members ?: emptyList(), createImages)
    }
    private suspend fun deleteMoimActivity(activityId: Long) {
        repository.deleteMoimActivity(activityId)
    }

    fun toggleIsParticipantVisible() {
        isParticipantVisible.value = !isParticipantVisible.value!!
    }

    private fun initDiaryState() {
        initialDiaryContent = diary.value?.content ?: ""
        initialImgList = diary.value?.diaryImages ?: emptyList()
        initialEnjoy = diary.value?.enjoyRating ?: 0
        Log.d("initDiaryState", "$initialDiaryContent, $initialImgList, $initialEnjoy")
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
