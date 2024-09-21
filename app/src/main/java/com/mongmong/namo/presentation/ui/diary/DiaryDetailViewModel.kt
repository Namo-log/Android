package com.mongmong.namo.presentation.ui.diary

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.PersonalDiary
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.FindCategoryUseCase
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) : ViewModel() {
    private val _diary = MutableLiveData<DiaryDetail>()
    val diary: LiveData<DiaryDetail> = _diary

    private val _diarySchedule = MutableLiveData<ScheduleForDiary>()
    val diarySchedule: LiveData<ScheduleForDiary> = _diarySchedule

    private val _diaryChanged = MutableLiveData<Boolean>(false)
    val diaryChanged: LiveData<Boolean> = _diaryChanged

    val content = MutableLiveData<String>()

    private val _enjoy = MutableLiveData<Int>()
    val enjoy: LiveData<Int> = _enjoy

    private val _imgList = MutableLiveData<List<DiaryImage>>(emptyList())
    val imgList: LiveData<List<DiaryImage>> = _imgList

    private val _addDiaryResult = MutableLiveData<Boolean>()
    val addDiaryResult: LiveData<Boolean> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<Boolean>()
    val editDiaryResult: LiveData<Boolean> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<Boolean>()
    val deleteDiaryResult: LiveData<Boolean> = _deleteDiaryResult

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialMoimDiaryContent: String? = null
    private var initialEnjoy: Int = 0

    private var isInitialLoadComplete = false

    var scheduleId: Long = 0

    private val _schedule = MutableLiveData<Schedule>(Schedule().getDefaultSchedule())
    val schedule: LiveData<Schedule> = _schedule

    private val _moimDiary = MutableLiveData<MoimDiary>()
    val moimDiary: LiveData<MoimDiary> = _moimDiary

    private val _isEdit = MutableLiveData<Boolean>(false)
    val isEdit: LiveData<Boolean> = _isEdit

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private var deleteImageIds = mutableListOf<Long>()

    init {
        content.observeForever { checkForChanges() }
        _imgList.observeForever { checkForChanges() }
        enjoy.observeForever{ checkForChanges() }
    }


    /** 개인 기록 **/
    // 기록 상단 일정 데이터 초기화
    fun getScheduleForDiary(scheduleId: Long) {
        this.scheduleId = scheduleId
        viewModelScope.launch {
            _diarySchedule.postValue(repository.getScheduleForDiary(scheduleId))
        }
    }

    // 개인 기록 개별 조회
    fun getPersonalDiary() {
        viewModelScope.launch {
            val result = repository.getPersonalDiary(scheduleId)
            _diary.postValue(result)
            Log.d("DiaryDetailViewModel getPersonalDiary", "$result")

            content.value = result.content
            _imgList.value = result.diaryImages
            _enjoy.value = result.enjoyRating

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

        content.value = ""
        _imgList.value = emptyList()
        _enjoy.value = 0

        initDiaryState()
        isInitialLoadComplete = true
    }


    // 개인 기록 추가
    fun addPersonalDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            val newImageUrls = uploadImageToS3UseCase.execute(
                PREFIX, (imgList.value ?: emptyList()).map { Uri.parse(it.imageUrl) }
            )

            _addDiaryResult.postValue(
                repository.addPersonalDiary(
                    content = content.value ?: "",
                    enjoyRating = enjoy.value ?: 3,
                    images = newImageUrls,
                    scheduleId = scheduleId
                )
            )
            deleteImageIds.clear()
        }
    }

    // 개인 기록 수정
    fun editPersonalDiary() {
        viewModelScope.launch {
            diary.value?.diaryId?.let { diaryId ->
                // 새로운 이미지 S3에 업로드
                val newImageUrls = uploadImageToS3UseCase.execute(
                    PREFIX,
                    imgList.value
                        ?.filter { it.diaryImageId == 0L }
                        ?.map { Uri.parse(it.imageUrl) }
                        ?: emptyList()
                )

                // 서버에 데이터 전송
                _editDiaryResult.postValue(
                    repository.editPersonalDiary(
                        content = content.value ?: "",
                        enjoyRating = enjoy.value ?: 3,
                        images = (
                                imgList.value
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
    fun deletePersonalDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.postValue(diary.value?.let { repository.deletePersonalDiary(it.diaryId) })
        }
    }

    /** 이미지 리스트 관련 로직 */
    fun addCreateImages(newImages: List<Uri>) {
        val currentImages = _imgList.value ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)
        _imgList.value = currentImages + newImagesToAdd.map {
            DiaryImage(diaryImageId = 0L, imageUrl = it.toString(), orderNumber = 1)
        }
    }

    fun removeImage(diaryImage: DiaryImage) {
        // 이미지 ID가 0이 아니면 삭제할 이미지, 아니라면 createImages에서 제거
        if (diaryImage.diaryImageId != 0L) deleteImageIds.add(diaryImage.diaryImageId)

        _imgList.value = _imgList.value?.filterNot { it.imageUrl == diaryImage.imageUrl }
    }

    fun updateImgList(newImgList: List<DiaryImage>) {
        _imgList.value = newImgList
        _diary.value?.diaryImages = newImgList
    }


    private fun initDiaryState() {
        initialDiaryContent = this.content.value
        initialImgList = _imgList.value ?: emptyList()
        initialEnjoy = _enjoy.value ?: 0
        Log.d("initDiaryState", "$initialDiaryContent, $initialImgList, $initialEnjoy")
    }

    private fun checkForChanges() {
        if (!isInitialLoadComplete) return
        _diaryChanged.value = (
                content.value != initialDiaryContent ||
                        imgList.value != initialImgList ||
                        enjoy.value != initialEnjoy
                )
        Log.d("initDiaryState", "${_diaryChanged.value}")
    }

    fun onEnjoyClicked(count: Int) {
        _enjoy.value = count
    }

    companion object {
        const val PREFIX = "diary"
    }
}
