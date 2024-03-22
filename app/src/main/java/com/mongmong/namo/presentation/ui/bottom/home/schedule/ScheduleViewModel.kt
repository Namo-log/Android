package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {
    private val _schedule = MutableLiveData<Event>()
    val schedule: LiveData<Event> = _schedule

    private val _scheduleList = MutableLiveData<List<Event>>(emptyList())
    val scheduleList: LiveData<List<Event>> = _scheduleList

    fun getDailySchedules(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getDailySchedules")
            _scheduleList.value = repository.getDailySchedules(startDate, endDate)
        }
    }

    fun getScheduleList() = _scheduleList.value

    fun addSchedule(schedule: Event) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "addSchedule $schedule")
            repository.addSchedule(
                schedule = schedule
            )
        }
    }

    fun editSchedule(schedule: Event) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "editSchedule $schedule")
            repository.editSchedule(
                schedule = schedule
            )
        }
    }

    fun deleteSchedule(localId: Long, serverId: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "deleteSchedule $schedule")
            repository.deleteSchedule(
                localId = localId,
                serverId = serverId
            )
        }
    }
}