package com.mongmong.namo.presentation.ui.home.schedule

import android.util.Log
import android.widget.TextView
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

    private val _scheduleList = MutableLiveData<List<GetMonthScheduleResult>>(emptyList())
    val scheduleList: LiveData<List<GetMonthScheduleResult>?> = _scheduleList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    private val _category = MutableLiveData<Category>()
    var category: LiveData<Category> = _category

    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    private val _prevClickedPicker = MutableLiveData<TextView?>()
    var prevClickedPicker: LiveData<TextView?> = _prevClickedPicker

    private val _monthDayList = MutableLiveData<List<DateTime>>()

    // 클릭한 날짜 처리
    private lateinit var _dailyScheduleList: List<GetMonthScheduleResult>

    private val _isShow = MutableLiveData(false)
    var isShow: LiveData<Boolean> = _isShow

    private var _prevIndex = -1 // 클릭한 날짜의 index
    private var _nowIndex = 0 // 클릭한 날짜의 index

    private val _clickedDatePair = MutableLiveData<Pair<Long, Long>>() // 클릭한 날짜의 시작, 종료 시간

    private val _isDailyScheduleEmptyPair = MutableLiveData<Pair<Boolean, Boolean>>()
    var isDailyScheduleEmptyPair: LiveData<Pair<Boolean, Boolean>> = _isDailyScheduleEmptyPair

    /** 월별 일정 리스트 조회 */
    fun getMonthSchedules(yearMonth: String) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "getMonthSchedules")
            _scheduleList.value = repository.getMonthSchedules(yearMonth)
        }
    }

//    /** 선택한 날짜의 일정 조회 */
//    fun getDailySchedules(startDate: Long, endDate: Long) {
//        viewModelScope.launch {
//            Log.d("ScheduleViewModel", "getDailySchedules")
//            _personalDailyScheduleList.value = repository.getDailySchedules(startDate, endDate)
//        }
//    }

    /** 일정 추가 */
    fun addSchedule() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "addSchedule ${_schedule.value}")
            _isComplete.postValue(
                repository.addSchedule(
                    schedule = _schedule.value!!.convertLocalScheduleToServer()
                )
            )
        }
    }

    /** 일정 수정 */
    fun editSchedule() {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "editSchedule ${_schedule.value}")
            _isComplete.postValue(
                repository.editSchedule(
                    scheduleId = _schedule.value!!.scheduleId,
                    schedule = _schedule.value!!.convertLocalScheduleToServer()
                )
            )
        }
    }

    /** 일정 삭제 */
    fun deleteSchedule(scheduleId: Long, isGroup: Boolean) {
        viewModelScope.launch {
            Log.d("ScheduleViewModel", "deleteSchedule $schedule")
            _isComplete.postValue(
                repository.deleteSchedule(
                    scheduleId = scheduleId,
                    isGroup = isGroup
                )
            )
        }
    }

    // 모임
    /** 모임 일정 카테고리 수정 */
    fun editMoimScheduleCategory() {
        viewModelScope.launch {
            _isComplete.postValue(
                repository.editMoimScheduleCategory(
                    PatchMoimScheduleCategoryRequestBody(
                        _schedule.value!!.scheduleId,
                        _schedule.value!!.categoryId
                    )
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
        }
    }

    /** 카테고리 id로 카테고리 조회 */
    fun findCategoryById() {
        viewModelScope.launch {
            // 카테고리 찾기
            _category.value = schedule.value?.let { schedule ->
                if (schedule.scheduleId == 0L && schedule.categoryId == 0L) { // 새 일정인 경우
//                    Log.d(
//                        "ScheduleViewModel",
//                        "findCategoryById() - categoryList:  ${_categoryList.value}"
//                    )
                    _categoryList.value?.first()
                } else {
                    findCategoryUseCase.invoke(schedule.categoryId, schedule.categoryId)
                }
            }
            setCategory()
//            Log.e("ScheduleViewModel", "findCategoryById() - category: ${_category.value}")
        }
    }

    /** 일정 정보 세팅 */
    fun setSchedule(schedule: Schedule?) {
        _schedule.value = schedule
        Log.d("ScheduleViewModel", "schedule: ${_schedule.value}")
        if (schedule?.placeName!!.isBlank()) {
            _schedule.value?.placeName = "없음"
        }
    }

    fun setCategory() {
        Log.d("ScheduleViewModel", "setCategory()")
        // 일정에 카테고리 정보 넣기
        _schedule.value = _schedule.value?.copy(
            categoryId = _category.value?.categoryId!!
        )
    }

    private fun setDailySchedule() {
        // 선택 날짜에 해당되는 일정 필터링
        _dailyScheduleList = _scheduleList.value!!.filter { schedule ->
            schedule.startDate <= _clickedDatePair.value!!.second &&
                    schedule.endDate >= _clickedDatePair.value!!.first
        }
        _isDailyScheduleEmptyPair.value = Pair(
            isDailyScheduleEmpty(false), // 개인 일정
            isDailyScheduleEmpty(true) // 모임 일정
        )
    }

    // 캘린더의 날짜 클릭
    fun clickDate(index: Int) {
        _nowIndex = index
        // 클릭한 날짜의 시작, 종료 시간 저장
        _clickedDatePair.value = Pair(
            (getClickedDate().withTimeAtStartOfDay().millis) / 1000, // 날짜 시작일
            (getClickedDate().plusDays(1).withTimeAtStartOfDay().millis - 1) / 1000, // 날짜 종료일
        )
        setDailySchedule()
    }

    fun updateIsShow() {
        _isShow.value = !_isShow.value!!
        _prevIndex = _nowIndex
    }

    // 일정 상세 바텀 시트 닫기 - 동일한 날짜를 다시 클릭했을 경우
    fun isCloseScheduleDetailBottomSheet() = _isShow.value == true && (_prevIndex == _nowIndex)

    // 캘린더에 들어갈 한달 날짜 리스트
    fun setMonthDayList(monthDayList: List<DateTime>) {
        _monthDayList.value = monthDayList
    }

    // 시간 변경
    fun updateTime(startDateTime: DateTime?, endDateTime: DateTime?) {
        _schedule.value = _schedule.value?.copy(
            startLong = startDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.startLong,
            endLong = endDateTime?.let { PickerConverter.parseDateTimeToLong(it) }
                ?: _schedule.value!!.endLong
        )
    }

    fun updatePlace(placeName: String, x: Double, y: Double) {
        _schedule.value = _schedule.value?.copy(
            placeName = placeName,
            placeX = x,
            placeY = y
        )
    }

    fun updatePrevClickedPicker(clicked: TextView?) {
        _prevClickedPicker.value = clicked
    }
    fun isMoimSchedule() = schedule.value!!.moimSchedule

    fun isCreateMode() = (schedule.value!!.scheduleId == 0L)

    private fun isDailyScheduleEmpty(isMoim: Boolean): Boolean {
        Log.d("ScheduleViewModel", "isDailyScheduleEmpty($isMoim): ${getDailySchedules(isMoim)}")
        return getDailySchedules(isMoim).isEmpty()
    }

    // 선택한 날짜
    fun getClickedDate() = _monthDayList.value!![_nowIndex]

    fun getDailySchedules(isMoim: Boolean): ArrayList<GetMonthScheduleResult> {
        return _dailyScheduleList.filter { schedule ->
            schedule.moimSchedule == isMoim
        } as ArrayList<GetMonthScheduleResult>
    }

    fun getDateTime(): Pair<DateTime, DateTime>? {
        if (_schedule.value != null) {
            return Pair(
                PickerConverter.parseLongToDateTime(schedule.value!!.startLong),
                PickerConverter.parseLongToDateTime(schedule.value!!.endLong)
            )
        }
        return null
    }

    fun getPlace(): Pair<String, LatLng>? {
        if (_schedule.value != null) {
            if (_schedule.value!!.placeX == 0.0 && _schedule.value!!.placeY == 0.0) return null
            return Pair(
                schedule.value!!.placeName,
                LatLng.from(schedule.value!!.placeY, schedule.value!!.placeX)
            )
        }
        return null
    }

    fun isInvalidDate(): Boolean {
        // 시작일 > 종료일
        return schedule.value!!.startLong > schedule.value!!.endLong
    }
}