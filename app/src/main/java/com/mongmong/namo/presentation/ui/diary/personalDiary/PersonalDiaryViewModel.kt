package com.mongmong.namo.presentation.ui.diary.personalDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.FindCategoryUseCase
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val findCategoryUseCase: FindCategoryUseCase
) : ViewModel() {
    private val _diary = MutableLiveData<Diary>()
    val diary: LiveData<Diary> = _diary

    private val _imgList = MutableLiveData<List<String>>(emptyList())
    val imgList: LiveData<List<String>> = _imgList

    private val _isDeleteComplete = MutableLiveData<Boolean>()
    val isDeleteComplete: LiveData<Boolean> = _isDeleteComplete

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category


    /** 개인 기록 개별 조회 **/
    fun getExistingPersonalDiary(diaryId: Long) {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel getDiary", "$diaryId")
            _diary.postValue(repository.getDiary(diaryId))
        }
    }
    /** 개인 기록 추가시 데이터 초기화 **/
    fun setNewPersonalDiary(schedule: Schedule, content: String) {
        _diary.value = Diary(
            diaryId = schedule.scheduleId,
            scheduleServerId = schedule.serverId,
            content = content,
            images = _imgList.value,
            state = RoomState.ADDED.state
        )
    }

    /** 개인 기록 추가 **/
    fun addPersonalDiary() {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            _diary.value?.let {
                repository.addDiary(
                    diary = it,
                    images = _imgList.value
                )
            }
        }
    }
    /** 개인 기록 수정 **/
    fun editPersonalDiary(content: String) {
        viewModelScope.launch {
            _diary.value?.let {
                it.content = content
                it.state = RoomState.EDITED.state
            }
            Log.d("PersonalDiaryViewModel editDiary", "${_diary.value}")
            _diary.value?.let {
                repository.editDiary(
                    diary = it,
                    images = _imgList.value
                )
            }
        }
    }
    /** 개인 기록 삭제 **/
    fun deletePersonalDiary(localId: Long, scheduleServerId: Long) {
        viewModelScope.launch {
            repository.deleteDiary(localId, scheduleServerId)
            _isDeleteComplete.postValue(true)
        }
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById(localId: Long, serverId: Long) {
        viewModelScope.launch {
            _category.value = findCategoryUseCase.invoke(localId, serverId)
        }
    }

    fun getImgList() = _imgList.value
    fun updateImgList(newImgList: List<String>) {
        _imgList.value = newImgList
        _diary.value?.images = _imgList.value
    }
}