package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    private val _schedule = MutableLiveData<GetMonthScheduleResult>()
    val schedule: LiveData<GetMonthScheduleResult> = _schedule

    private val _scheduleList = MutableLiveData<List<GetMonthScheduleResult>>(emptyList())
    val scheduleList: LiveData<List<GetMonthScheduleResult>?> = _scheduleList

    // 개인쪽
    /** 월별 모임 일정 조회 */
    fun getMonthMoimSchedule(yearMonth: String) {
        viewModelScope.launch {
            Log.d("MoimScheduleViewModel", "getMonthMoimSchedule")
            _scheduleList.value = repository.getMonthMoimSchedule(yearMonth)
        }
    }

    // 그룹쪽
    /** 모임 일정 생성 */
    fun postMoimSchedule(moimSchedule: AddMoimSchedule) {
        viewModelScope.launch {
            repository.addMoimSchedule(moimSchedule = moimSchedule)
        }
    }

    /** 모임 일정 수정 */
    fun editMoimSchedule(moimSchedule: EditMoimSchedule) {
        viewModelScope.launch {
            repository.editMoimSchedule(moimSchedule = moimSchedule)
        }
    }

    /** 모임 일정 삭제 */
    fun deleteMoimSchedule(moimScheduleId: Long) {
        viewModelScope.launch {
            repository.deleteMoimSchedule(moimScheduleId)
        }
    }
}