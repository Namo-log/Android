package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.diary.AddDiaryUseCase
import com.mongmong.namo.domain.usecase.diary.EditDiaryUseCase
import com.mongmong.namo.domain.usecase.diary.GetDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val addDiaryUseCase: AddDiaryUseCase,
    private val editDiaryUseCase: EditDiaryUseCase,
    private val getDiaryUseCase: GetDiaryUseCase,
    private val repository: DiaryRepository
) : ViewModel() {
    private val _diary = MutableLiveData<Diary>()
    val diary: LiveData<Diary> = _diary

    private val _getCategoryResult = MutableLiveData<Category>()
    val getCategoryResult: LiveData<Category> = _getCategoryResult

    private val _imgList = MutableLiveData<List<String>>(emptyList())
    val imgList: LiveData<List<String>> = _imgList
    fun setNewDiary(event: Event, content: String) {
        _diary.value = Diary(
            diaryId = event.eventId,
            serverId = event.serverIdx,
            content = content,
            images = _imgList.value,
            state = R.string.event_current_added.toString()
        )
    }
    fun getExistingDiary(diaryId: Long) {
        viewModelScope.launch {
            Log.d("DiaryViewModel getDiary", "$diaryId")
            _diary.postValue(getDiaryUseCase.invoke(diaryId))
        }
    }

    fun addDiary(images: List<File>?) {
        viewModelScope.launch {
            Log.d("DiaryViewModel addDiary", "$diary")
            _diary.value?.let {
                addDiaryUseCase(
                    diary = it,
                    images = images
                )
            }
        }
    }

    fun editDiary(content: String, images: List<File>?) {
        viewModelScope.launch {
            _diary.value?.let {
                it.content = content
                it.state = R.string.event_current_edited.toString()
            }
            Log.d("DiaryViewModel editDiary", "${_diary.value}")
            _diary.value?.let {
                editDiaryUseCase(
                    diary = it,
                    images = images
                )
            }
        }
    }

    fun deleteDiary(localId: Long, scheduleServerId: Long) {
        viewModelScope.launch {
            repository.deleteDiary(localId, scheduleServerId)
        }
    }

    fun getImgList() = _imgList.value
    fun updateImgList(newImgList: List<String>) {
        _imgList.value = newImgList
        _diary.value?.images = _imgList.value
    }

    companion object {
        const val EVENT_CURRENT_ADDED = "ADDED"
        const val EVENT_CURRENT_EDITED = "EDITED"
    }
}