package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.GetMonthScheduleResult
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
}