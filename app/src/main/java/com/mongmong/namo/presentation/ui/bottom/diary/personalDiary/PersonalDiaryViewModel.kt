package com.mongmong.namo.presentation.ui.bottom.diary.personalDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PersonalDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _diary = MutableLiveData<Diary>()
    val diary: LiveData<Diary> = _diary

    private val _imgList = MutableLiveData<List<String>>(emptyList())
    val imgList: LiveData<List<String>> = _imgList

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
    fun addPersonalDiary(images: List<File>?) {
        viewModelScope.launch {
            Log.d("PersonalDiaryViewModel addDiary", "$_diary")
            _diary.value?.let {
                repository.addDiary(
                    diary = it,
                    images = images
                )
            }
        }
    }
    /** 개인 기록 수정 **/
    fun editPersonalDiary(content: String, images: List<File>?) {
        viewModelScope.launch {
            _diary.value?.let {
                it.content = content
                it.state = RoomState.EDITED.state
            }
            Log.d("PersonalDiaryViewModel editDiary", "${_diary.value}")
            _diary.value?.let {
                repository.editDiary(
                    diary = it,
                    images = images
                )
            }
        }
    }
    /** 개인 기록 삭제 **/
    fun deletePersonalDiary(localId: Long, scheduleServerId: Long) {
        viewModelScope.launch {
            repository.deleteDiary(localId, scheduleServerId)
        }
    }

    fun getImgList() = _imgList.value
    fun updateImgList(newImgList: List<String>) {
        _imgList.value = newImgList
        _diary.value?.images = _imgList.value
    }
}