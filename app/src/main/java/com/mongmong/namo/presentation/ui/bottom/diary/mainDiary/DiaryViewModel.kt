package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.AddDiaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val addDiaryUseCase: AddDiaryUseCase
) : ViewModel() {
    private val _diaryAddedStatus = MutableLiveData<Boolean>()
    val diaryAddedStatus: LiveData<Boolean> = _diaryAddedStatus
    fun addDiary(diary: Diary, images: List<File>?) {
        viewModelScope.launch {
            if (diary.content == null) {
                addDiaryUseCase(
                    diary = diary,
                    diaryLocalId = diary.diaryId,
                    content = "",
                    images = images,
                    serverId = diary.serverId
                )

            } else {
                addDiaryUseCase(
                    diary = diary,
                    diaryLocalId = diary.diaryId,
                    content = diary.content!!,
                    images = images,
                    serverId = diary.serverId
                )
            }
        }
    }
}