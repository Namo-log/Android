package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.domain.usecase.FindCategoryUseCase
import com.mongmong.namo.domain.usecase.GetCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val findCategoryUseCase: FindCategoryUseCase
) : ViewModel() {
    private val _schedule = MutableLiveData<Schedule>()
    val schedule: LiveData<Schedule> = _schedule

    private val _personalDailyScheduleList = MutableLiveData<List<Schedule>>(emptyList())
    val personalDailyScheduleList: LiveData<List<Schedule>?> = _personalDailyScheduleList

    private val _personalScheduleList = MutableLiveData<List<Schedule>>(emptyList())
    val personalScheduleList: LiveData<List<Schedule>?> = _personalScheduleList

    private val _moimScheduleList = MutableLiveData<List<GetMonthScheduleResult>>(emptyList())
    val moimScheduleList: LiveData<List<GetMonthScheduleResult>?> = _moimScheduleList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    /** 월별 일정 리스트 조회 */
    fun getMonthSchedules(monthStart: Long, monthEnd: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getMonthSchedules")
            _personalScheduleList.value = repository.getMonthSchedules(monthStart, monthEnd)
        }
    }

    /** 선택한 날짜의 일정 조회 */
    fun getDailySchedules(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getDailySchedules")
            _personalDailyScheduleList.value = repository.getDailySchedules(startDate, endDate)
        }
    }

    /** 일정 추가 */
    fun addSchedule(schedule: Schedule) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "addSchedule $schedule")
            repository.addSchedule(
                schedule = schedule
            )
            _isComplete.postValue(true)
        }
    }

    /** 일정 수정 */
    fun editSchedule(schedule: Schedule) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "editSchedule $schedule")
            repository.editSchedule(
                schedule = schedule
            )
        }
    }

    /** 일정 삭제 */
    fun deleteSchedule(localId: Long, serverId: Long) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "deleteSchedule $schedule")
            repository.deleteSchedule(
                localId = localId,
                serverId = serverId
            )
        }
    }

    // 모임
    /** 월별 모임 일정 조회 */
    fun getMonthMoimSchedule(yearMonth: String) {
        viewModelScope.launch {
            Log.d("MoimScheduleViewModel", "getMonthMoimSchedule")
            _moimScheduleList.value = repository.getMonthMoimSchedule(yearMonth)
        }
    }

    /** 모임 일정 카테고리 수정 */
    fun editMoimScheduleCategory(scheduleId: Long, categoryId: Long) {
        viewModelScope.launch {
            repository.editMoimScheduleCategory(PatchMoimScheduleCategoryBody(scheduleId, categoryId))
        }
    }

    /** 모임 일정 알림 수정 */
    fun editMoimScheduleAlert(scheduleId: Long, alertList: List<Int>) {
        viewModelScope.launch {
            repository.editMoimScheduleAlert(MoimScheduleAlarmBody(scheduleId, alertList))
        }
    }

    /** 카테고리 조회 */
    fun getCategories() {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "getCategories")
            _categoryList.value = getCategoriesUseCase.invoke()
        }
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById(localId: Long, serverId: Long) {
        viewModelScope.launch {
            _category.value = findCategoryUseCase.invoke(localId, serverId)
        }
    }
}