package com.mongmong.namo.presentation.ui.community.moim

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.MoimPreview
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimViewModel @Inject constructor(
    private val repository: ScheduleRepository
): ViewModel() {
    private val _moimPreviewList = MutableLiveData<List<MoimPreview>>(emptyList())
    val moimPreviewList: LiveData<List<MoimPreview>> = _moimPreviewList

    init {
        getMoim()
    }

    /** 모임 일정 목록 조회 */
    fun getMoim() {
        viewModelScope.launch {
            _moimPreviewList.value = repository.getMoimSchedules()
        }
    }
}