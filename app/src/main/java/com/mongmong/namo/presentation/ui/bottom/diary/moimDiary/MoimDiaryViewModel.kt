package com.mongmong.namo.presentation.ui.bottom.diary.moimDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.MoimDiaryResult
import com.mongmong.namo.domain.repositories.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _getMoimDiaryResult = MutableLiveData<MoimDiaryResult>()
    val getMoimDiaryResult : LiveData<MoimDiaryResult> = _getMoimDiaryResult

    private val _patchDiaryResult = MutableLiveData<Boolean>()
    val patchDiaryResult : LiveData<Boolean> = _patchDiaryResult

    private val _memo = MutableLiveData<String>()

    /** 모임 기록 개별 조회 **/
    fun getMoimDiary(scheduleId: Long) {
        viewModelScope.launch {
            Log.d("DiaryViewModel getMoimDiary", "$scheduleId")
            _getMoimDiaryResult.postValue(repository.getMoimDiary(scheduleId))
        }
    }


    /** 모임 기록 수정 **/
    fun patchMoimDiary(scheduleId: Long, content: String) {
        viewModelScope.launch {
            _patchDiaryResult.postValue(repository.patchMoimDiary(scheduleId, content))
        }
    }

    fun setMemo(memo: String) { _memo.value = memo }
    fun getMemo() = _memo.value
}