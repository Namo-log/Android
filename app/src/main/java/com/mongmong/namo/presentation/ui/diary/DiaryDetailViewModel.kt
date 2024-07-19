package com.mongmong.namo.presentation.ui.diary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.PersonalDiary
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.FindCategoryUseCase
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val findCategoryUseCase: FindCategoryUseCase
) : ViewModel() {
    private val _diary = MutableLiveData<PersonalDiary>()
    val diary: LiveData<PersonalDiary> = _diary

    private val _schedule = MutableLiveData<Schedule>(Schedule().getDefaultSchedule())
    val schedule: LiveData<Schedule> = _schedule

    private val _moimDiary = MutableLiveData<MoimDiary>()
    val moimDiary: LiveData<MoimDiary> = _moimDiary

    private val _isEdit = MutableLiveData<Boolean>(false)
    val isEdit: LiveData<Boolean> = _isEdit

    private var isInitialLoad = true

    private val _imgList = MutableLiveData<List<DiaryImage>>(emptyList())
    val imgList: LiveData<List<DiaryImage>> = _imgList

    private val _addDiaryResult = MutableLiveData<DiaryAddResponse>()
    val addDiaryResult: LiveData<DiaryAddResponse> = _addDiaryResult

    private val _editDiaryResult = MutableLiveData<DiaryResponse>()
    val editDiaryResult: LiveData<DiaryResponse> = _editDiaryResult

    private val _deleteDiaryResult = MutableLiveData<DiaryResponse>()
    val deleteDiaryResult: LiveData<DiaryResponse> = _deleteDiaryResult

    private val _deleteMemoResult = MutableLiveData<Boolean>()
    val deleteMemoResult: LiveData<Boolean> = _deleteMemoResult

    private val _patchMemoResult = MutableLiveData<Boolean>()
    val patchMemoResult : LiveData<Boolean> = _patchMemoResult

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _createImages = MutableLiveData<List<String>>(emptyList())
    val createImages: LiveData<List<String>> = _createImages

    private val _deleteImageIds = MutableLiveData<List<Int>>(emptyList())
    val deleteImageIds: LiveData<List<Int>> = _deleteImageIds

    private var initialDiaryContent: String? = null
    private var initialImgList: List<DiaryImage> = emptyList()
    private var initialMoimDiaryContent: String? = null

    /** 개인 기록 **/
    // 개인 기록 개별 조회
    fun getExistingPersonalDiary(schedule: Schedule) {
        viewModelScope.launch {
            val result = repository.getPersonalDiary(schedule.scheduleId).result
            Log.d("DiaryDetailViewModel getDiary", "$result")
            _schedule.value = schedule
            _diary.value = PersonalDiary(
                diaryId = schedule.scheduleId,
                scheduleServerId = schedule.serverId,
                _content = result.contents,
                images = result.images,
                state = RoomState.ADDED.state
            )
            _imgList.value = result.images // 기존 이미지 설정
            initDiaryState(result.contents, result.images) // 초기 상태 저장
        }
    }

    // 개인 기록 추가시 데이터 초기화
    fun setNewPersonalDiary(schedule: Schedule) {
        _schedule.value = schedule
        _diary.value = PersonalDiary(
            diaryId = schedule.serverId,
            scheduleServerId = schedule.serverId,
            _content = "",
            images = emptyList(),
            state = RoomState.ADDED.state
        )
        initDiaryState("", emptyList()) // 초기 상태 저장
    }

    // 개인 기록 추가
    fun addPersonalDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            _diary.value?.let {
                _addDiaryResult.postValue(repository.addPersonalDiary(
                    diary = it,
                    images = _createImages.value
                ))
            }
        }
    }

    // 개인 기록 수정
    fun editPersonalDiary() {
        viewModelScope.launch {
            _diary.value?.let {
                it.state = RoomState.EDITED.state
            }
            Log.d("PersonalDiaryViewModel editDiary", "${_diary.value}")
            _diary.value?.let {
                _editDiaryResult.postValue(repository.editPersonalDiary(
                    diary = it,
                    images = _createImages.value,
                    deleteImageIds = _deleteImageIds.value
                ))
            }
        }
    }

    // 개인 기록 삭제
    fun deletePersonalDiary() {
        viewModelScope.launch {
            _deleteDiaryResult.postValue(schedule.value?.let { repository.deletePersonalDiary(it.scheduleId) })
        }
    }

    fun updateImgList(newImgList: List<DiaryImage>) {
        _imgList.value = newImgList
        _diary.value?.images = newImgList
    }

    fun addCreateImages(newImages: List<String>) {
        val currentImages = _imgList.value ?: emptyList()
        val newImagesToAdd = newImages.take(3 - currentImages.size)
        _createImages.value = (_createImages.value ?: emptyList()) + newImagesToAdd
        _imgList.value = currentImages + newImagesToAdd.map { DiaryImage(id = 0, url = it) }
    }

    fun addDeleteImageId(imageId: Int) {
        _deleteImageIds.value = (_deleteImageIds.value ?: emptyList()) + imageId
        _imgList.value = _imgList.value?.filterNot { it.id == imageId }
    }

    // 초기 상태 저장 메서드
    private fun initDiaryState(content: String?, images: List<DiaryImage>) {
        initialDiaryContent = content
        initialImgList = images
    }

    // 변경 여부 확인 메서드
    fun isDiaryChanged(): Boolean {
        return _diary.value?.content != initialDiaryContent || _imgList.value != initialImgList
    }

    /** 모임 기록*/
    // 모임 메모 조회
    fun getMoimMemo(scheduleId: Long) {
        viewModelScope.launch {
            val moimMemo = repository.getMoimMemo(scheduleId)
            _moimDiary.postValue(moimMemo)
            initMoimDiaryState(moimMemo.content) // 초기 상태 저장
        }
    }

    fun isEditMode() {
        if (isInitialLoad) {
            _isEdit.value = !_moimDiary.value?.content.isNullOrEmpty()
            isInitialLoad = false
            Log.d("getMoimMemo", "${_isEdit.value} , $isInitialLoad")
        }
    }

    // 모임 메모 수정
    fun patchMoimMemo(scheduleId: Long) {
        viewModelScope.launch {
            _patchMemoResult.postValue(repository.patchMoimMemo(scheduleId, _moimDiary.value?.content ?: ""))
        }
    }

    // 모임 메모 삭제
    fun deleteMoimMemo(scheduleId: Long) {
        viewModelScope.launch {
            Log.d("MoimDiaryViewModel deleteMoimMemo", "$scheduleId")
            _deleteMemoResult.value = repository.deleteMoimMemo(scheduleId)
        }
    }

    // 초기 상태 저장 메서드
    private fun initMoimDiaryState(content: String?) {
        initialMoimDiaryContent = content
    }

    // 변경 여부 확인 메서드
    fun isMoimDiaryChanged(): Boolean {
        return _moimDiary.value?.content != initialMoimDiaryContent
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById() {
        viewModelScope.launch {
            _category.value =
                schedule.value?.let {
                    Log.d("findCategoryById", "${_schedule.value}")
                    findCategoryUseCase.invoke(it.categoryId, it.categoryId)
                }
            Log.d("findCategoryById", "${_category.value}")
        }
    }
}
