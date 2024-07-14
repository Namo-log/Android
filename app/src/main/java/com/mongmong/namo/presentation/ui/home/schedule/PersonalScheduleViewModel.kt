package com.mongmong.namo.presentation.ui.home.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kakao.vectormap.LatLng
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.domain.usecase.FindCategoryUseCase
import com.mongmong.namo.domain.usecase.GetCategoriesUseCase
import com.mongmong.namo.presentation.utils.PickerConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject

@HiltViewModel
class PersonalScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val findCategoryUseCase: FindCategoryUseCase
) : ViewModel() {
    private val _schedule = MutableLiveData<Schedule?>()
    val schedule: LiveData<Schedule?> = _schedule

    private val _personalDailyScheduleList = MutableLiveData<List<Schedule>>(emptyList())
    val personalDailyScheduleList: LiveData<List<Schedule>?> = _personalDailyScheduleList

    private val _moimScheduleList = MutableLiveData<List<GetMonthScheduleResult>>(emptyList())
    val moimScheduleList: LiveData<List<GetMonthScheduleResult>?> = _moimScheduleList

    private val _scheduleList = MutableLiveData<List<GetMonthScheduleResult>>(emptyList())
    val scheduleList: LiveData<List<GetMonthScheduleResult>?> = _scheduleList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    private val _category = MutableLiveData<Category>()
    var category: LiveData<Category> = _category

    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    /** 월별 일정 리스트 조회 */
    fun getMonthSchedules(yearMonth: String) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getMonthSchedules")
            _scheduleList.value = repository.getMonthSchedules(yearMonth)
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
    fun addSchedule() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "addSchedule ${_schedule.value}")
            repository.addSchedule(
                schedule = _schedule.value!!.convertLocalScheduleToServer()
            )
            _isComplete.postValue(true)
        }
    }

    /** 일정 수정 */
    fun editSchedule() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "editSchedule ${_schedule.value}")
            repository.editSchedule(
                scheduleId = _schedule.value!!.scheduleId,
                schedule = _schedule.value!!.convertLocalScheduleToServer()
            )
        }
    }

    /** 일정 삭제 */
    fun deleteSchedule(scheduleId: Long, isGroup: Boolean) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "deleteSchedule $schedule")
            repository.deleteSchedule(
                scheduleId = scheduleId,
                isGroup = isGroup
            )
        }
    }

    // 모임
    /** 월별 모임 일정 조회 */
    fun getMonthMoimSchedule(yearMonth: String) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getMonthMoimSchedule")
            _moimScheduleList.value = repository.getMonthMoimSchedule(yearMonth)
        }
    }

    /** 모임 일정 카테고리 수정 */
    fun editMoimScheduleCategory() {
        viewModelScope.launch {
            repository.editMoimScheduleCategory(
                PatchMoimScheduleCategoryRequestBody(
                    _schedule.value!!.scheduleId,
                    _schedule.value!!.categoryId
                )
            )
        }
    }

    /** 모임 일정 알림 수정 */
    fun editMoimScheduleAlert(scheduleId: Long, alertList: List<Int>) {
        viewModelScope.launch {
            repository.editMoimScheduleAlert(
                PatchMoimScheduleAlarmRequestBody(
                    scheduleId,
                    alertList
                )
            )
        }
    }

    /** 카테고리 조회 */
    fun getCategories() {
        viewModelScope.launch {
            _categoryList.value = getCategoriesUseCase.invoke()
            Log.d("ScheduleViewModel", "getCategories() - categoryList: ${categoryList.value}")
        }
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "findCategoryById()")
            // 카테고리 찾기
            _category.value = schedule.value?.let { schedule ->
                if (schedule.scheduleId == 0L && schedule.categoryId == 0L) { // 새 일정인 경우
                    Log.d(
                        "ScheduleViewModel",
                        "findCategoryById() - categoryList:  ${_categoryList.value}"
                    )
                    _categoryList.value?.first()
                } else {
                    findCategoryUseCase.invoke(schedule.categoryId, schedule.categoryId)
                }
            }
            setCategory()
            Log.e("ScheduleViewModel", "findCategoryById() - category: ${_category.value}")
        }
    }

    /** 일정 정보 세팅 */
    fun setSchedule(schedule: Schedule?) {
        viewModelScope.launch {
            _schedule.value = schedule
            Log.d("ScheduleViewModel", "schedule: ${_schedule.value}")
        }
    }

    fun setCategory() {
        Log.d("ScheduleViewModel", "setCategory()")
        // 일정에 카테고리 정보 넣기
        _schedule.value = _schedule.value?.copy(
            categoryId = _category.value?.categoryId!!
        )
    }

    fun updateTitle(title: String) {
        _schedule.value = _schedule.value?.copy(
            title = title
        )
    }

    // 시간 변경
    fun updateTime(startDateTime: DateTime?, endDateTime: DateTime?) {
        Log.d("ScheduleViewModel", "setTime()\nstart: $startDateTime\nend: $endDateTime")
        _schedule.value = _schedule.value?.copy(
            startLong = startDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.startLong,
            endLong = endDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.endLong
        )
        Log.d(
            "ScheduleViewModel",
            "startLong: ${_schedule.value!!.startLong}\nendLong: ${_schedule.value!!.endLong}"
        )
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _schedule.value = _schedule.value?.copy(
            placeName = placeName,
            placeX = x,
            placeY = y
        )
        Log.d("ScheduleViewModel", "updatePlace() - $placeName, $x, $y")
    }

    fun getScheduleCategoryId() = _schedule.value!!.categoryId

    fun isMoimSchedule() = _schedule.value!!.moimSchedule

    fun isCreateMode() = (_schedule.value!!.scheduleId == 0L)

    fun getDateTime(): Pair<DateTime, DateTime>? {
        Log.d("ScheduleViewModel", "getDateTime()")
        if (_schedule.value != null) {
            return Pair(
                PickerConverter.parseLongToDateTime(_schedule.value!!.startLong),
                PickerConverter.parseLongToDateTime(_schedule.value!!.endLong)
            )
        }
        return null
    }

    fun getPlace(): Pair<String, LatLng>? {
        Log.d("ScheduleViewModel", "getPlace()")
        if (_schedule.value != null) {
            if (_schedule.value!!.placeX == 0.0 && _schedule.value!!.placeY == 0.0) return null
            return Pair(
                _schedule.value!!.placeName,
                LatLng.from(_schedule.value!!.placeY, _schedule.value!!.placeX)
            )
        }
        return null
    }

    fun isInvalidDate(): Boolean {
        // 시작일 > 종료일
        return _schedule.value!!.startLong > _schedule.value!!.endLong
    }
}