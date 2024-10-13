package com.mongmong.namo.presentation.ui.diary

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
) : ViewModel() {
    private val _diary = MutableLiveData<DiaryDetail>()
    val diary: LiveData<DiaryDetail> = _diary

    private val _diarySchedule = MutableLiveData<ScheduleForDiary>()
    val diarySchedule: LiveData<ScheduleForDiary> = _diarySchedule

    private val _diaryChanged = MutableLiveData<Boolean>(false)
    val diaryChanged: LiveData<Boolean> = _diaryChanged

    private val _addDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val addDiaryResult: LiveData<DiaryBaseResponse> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val editDiaryResult: LiveData<DiaryBaseResponse> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<DiaryBaseResponse>()
    val deleteDiaryResult: LiveData<DiaryBaseResponse> = _deleteDiaryResult

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialEnjoy: Int = 0

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    private var deleteImageIds = mutableListOf<Long>()

    // 기록 상단 일정 데이터 초기화
    fun getScheduleForDiary(scheduleId: Long) {
        this.scheduleId = scheduleId
        viewModelScope.launch {
            _diarySchedule.postValue(repository.getScheduleForDiary(scheduleId))
        }
    }

    // 개인 기록 개별 조회
    fun getDiaryData() {
        viewModelScope.launch {
            val result = repository.getDiary(scheduleId)
            _diary.value = result
            Log.d("DiaryDetailViewModel getPersonalDiary", "$result")

            initDiaryState() // 초기 상태 저장
            deleteImageIds.clear()
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
    }


    // 개인 기록 추가
    fun addDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            val newImageUrls = uploadImageToS3UseCase.execute(
                PREFIX, (diary.value?.diaryImages ?: emptyList()).map { Uri.parse(it.imageUrl) }
            )

            _addDiaryResult.value =
                repository.addDiary(
                    content = diary.value?.content ?: "",
                    enjoyRating = diary.value?.enjoyRating ?: 3,
                    images = newImageUrls,
                    scheduleId = scheduleId
                )
        }
    }

    // 개인 기록 수정
    fun editDiary() {
        viewModelScope.launch {
            diary.value?.diaryId?.let { diaryId ->
                // 새로운 이미지 S3에 업로드
                val newImageUrls = uploadImageToS3UseCase.execute(
                    PREFIX,
                    diary.value?.diaryImages
                        ?.filter { it.diaryImageId == 0L }
                        ?.map { Uri.parse(it.imageUrl) }
                        ?: emptyList()
                )

                // 서버에 데이터 전송
                _editDiaryResult.value =
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
            }
        }
    }


    // 개인 기록 삭제
    fun deleteDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.postValue(diary.value?.let { repository.deleteDiary(it.diaryId) })
        }
    }

    /** 이미지 리스트 관련 로직 */
    fun addImages(newImages: List<Uri>) {
        val currentImages = diary.value?.diaryImages ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)
        _diary.value?.diaryImages = currentImages + newImagesToAdd.map {
            DiaryImage(diaryImageId = 0L, imageUrl = it.toString(), orderNumber = 1)
        }

        _diary.value = _diary.value
        checkForChanges()
    }

    fun removeImage(diaryImage: DiaryImage) {
        if (diaryImage.diaryImageId != 0L) deleteImageIds.add(diaryImage.diaryImageId)

        _diary.value?.diaryImages = diary.value?.diaryImages?.filterNot { it.imageUrl == diaryImage.imageUrl }!!
        checkForChanges()
    }


    private fun initDiaryState() {
        initialDiaryContent = _diary.value?.content
        initialImgList = _diary.value?.diaryImages ?: emptyList()
        initialEnjoy = _diary.value?.enjoyRating ?: 0

        isInitialLoadComplete = true
    }

    fun checkForChanges() {
        if(!isInitialLoadComplete) return
        _diaryChanged.value = (
                diary.value?.content != initialDiaryContent ||
                        diary.value?.diaryImages != initialImgList ||
                        diary.value?.enjoyRating != initialEnjoy
                )
        Log.d("initDiaryState", "${_diary?.value?.content} \n${initialDiaryContent}\n${initialEnjoy}\n${initialImgList}")
    }

    fun updateEnjoy(count: Int) {
        diary.value?.enjoyRating = count
        checkForChanges()
    }

    companion object {
        const val PREFIX = "diary"
    }
}
