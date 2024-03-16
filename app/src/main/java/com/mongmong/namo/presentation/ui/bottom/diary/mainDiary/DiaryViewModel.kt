package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Event
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
    private val getDiaryUseCase: GetDiaryUseCase
) : ViewModel() {
    private val _getDiaryResult = MutableLiveData<Diary>()
    val getDiaryResult: LiveData<Diary> = _getDiaryResult

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    fun getDiary(diaryId: Long) {
        viewModelScope.launch {
            Log.d("DiaryViewModel getDiary", "$diaryId")
            _getDiaryResult.postValue(getDiaryUseCase.invoke(diaryId))
        }
    }

    fun addDiary(diary: Diary, images: List<File>?) {
        viewModelScope.launch {
            Log.d("DiaryViewModel addDiary", "$diary")
            addDiaryUseCase(
                diary = diary,
                images = images
            )
        }
    }

    fun editDiary(diary: Diary, images: List<File>?) {
        viewModelScope.launch {
            Log.d("DiaryViewModel editDiary", "$diary")
            editDiaryUseCase(
                diary = diary,
                images = images
            )
        }
    }
}