package com.mongmong.namo.presentation.ui.diary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetMoimMemoResponse
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.FindCategoryUseCase
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiaryDetailViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val findCategoryUseCase: FindCategoryUseCase
) : ViewModel() {
    private val _diary = MutableLiveData<Diary>()
    val diary: LiveData<Diary> = _diary

    private val _schedule = MutableLiveData<Schedule>(Schedule().getDefaultSchedule())
    val schedule: LiveData<Schedule> = _schedule

    private val _imgList = MutableLiveData<List<String>>(emptyList())
    val imgList: LiveData<List<String>> = _imgList

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

    private val _memo = MutableLiveData<String>()
    val memo: LiveData<String> = _memo

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _getMoimMemoResponse = MutableLiveData<GetMoimMemoResponse>()
    val getMoimMemoResponse: LiveData<GetMoimMemoResponse> = _getMoimMemoResponse

    /** 개인 기록 **/
    // 개인 기록 개별 조회
    fun getExistingPersonalDiary(schedule: Schedule) {
        viewModelScope.launch {
            val result = repository.getPersonalDiary(schedule.scheduleId).result
            Log.d("DiaryDetailViewModel getDiary", "$result")
            _schedule.value = schedule
            _diary.value = Diary(
                diaryId = schedule.scheduleId,
                scheduleServerId = schedule.serverId,
                _content = result.contents,
                images = result.urls,
                state = RoomState.ADDED.state
            )
        }
    }
    // 개인 기록 추가시 데이터 초기화
    fun setNewPersonalDiary(schedule: Schedule) {
        _schedule.value = schedule
        _diary.value = Diary(
            diaryId = schedule.serverId,
            scheduleServerId = schedule.serverId,
            _content = "",
            images = _imgList.value,
            state = RoomState.ADDED.state
        )
    }

    // 개인 기록 추가
    fun addPersonalDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            _diary.value?.let {
                _addDiaryResult.postValue(repository.addPersonalDiary(
                    diary = it,
                    images = _imgList.value
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
                    images = _imgList.value
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

    /** 모임 기록*/
    // 모임 메모 조회
    fun getMoimMemo(scheduleId: Long) {
        viewModelScope.launch {
            _getMoimMemoResponse.postValue(repository.getMoimMemo(scheduleId))
        }
    }

    // 모임 메모 수정
    fun patchMoimMemo(scheduleId: Long, content: String) {
        viewModelScope.launch {
            _patchMemoResult.postValue(repository.patchMoimMemo(scheduleId, content))
        }
    }

    // 모임 메모 삭제
    fun deleteMoimMemo(scheduleId: Long) {
        viewModelScope.launch {
            Log.d("MoimDiaryViewModel deleteMoimMemo", "$scheduleId")
            _deleteMemoResult.value = repository.deleteMoimMemo(scheduleId)
        }
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById() {
        viewModelScope.launch {
            _category.value =
                schedule.value?.let { findCategoryUseCase.invoke(it.categoryId, it.categoryId) }
        }
    }

    fun getImgList() = _imgList.value
    fun updateImgList(newImgList: List<String>) {
        _imgList.value = newImgList
        _diary.value?.images = _imgList.value
    }

    fun setMemo(memo: String) { _memo.value = memo }
    fun getMemo() = _memo.value

    fun getFormattedMonth(): String =
        _schedule.value?.let { DateTime(_schedule.value!!.startLong * 1000).toString("MMM", Locale.ENGLISH) }
            .orEmpty()

    fun getFormattedDay(): String =
        _schedule.value.let { DateTime(_schedule.value!!.startLong * 1000).toString("dd") }
            .orEmpty()

    fun getFormattedDate(): String =
        _schedule.value.let { DateTime(_schedule.value!!.startLong * 1000).toString("yyyy.MM.dd (EE) hh:mm") }
            .orEmpty()
}