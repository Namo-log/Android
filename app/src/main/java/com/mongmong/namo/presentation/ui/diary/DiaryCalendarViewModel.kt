package com.mongmong.namo.presentation.ui.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.domain.model.CalendarDiaryDate
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiaryCalendarViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _diariesByDate = MutableLiveData<List<Diary>>()
    val diariesByDate: LiveData<List<Diary>> = _diariesByDate

    private val _isBottomSheetOpened = MutableLiveData(false)
    val isBottomSheetOpened: LiveData<Boolean> = _isBottomSheetOpened

    private val _dateTitle = MutableLiveData<String>()
    val dateTitle: LiveData<String> = _dateTitle

    private var _selectedDate = CalendarDay(0, 0, 0, false)
    val selectedDate: CalendarDay = _selectedDate

    private val _calendarDiaryResult = MutableLiveData<CalendarDiaryDate>()
    val calendarDiaryResult: LiveData<CalendarDiaryDate> = _calendarDiaryResult

    private val _isReturnBtnVisible = MutableLiveData<Boolean>(false)
    val isReturnBtnVisible: LiveData<Boolean> = _isReturnBtnVisible


    fun toggleBottomSheetState() {
        _isBottomSheetOpened.value = _isBottomSheetOpened.value != true
    }

    fun setReturnBtnVisible(visible: Boolean) {
        _isReturnBtnVisible.value = visible
    }

    fun setSelectedDate(date: CalendarDay) {
        _selectedDate = date

        val calendar = Calendar.getInstance().apply {
            set(date.year, date.month, date.date)
        }
        val dateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())

        _dateTitle.value = dateFormat.format(calendar.time)
    }

    fun getCalendarDiary(yearMonth: String) {
        viewModelScope.launch {
            _calendarDiaryResult.postValue(repository.getCalendarDiary(yearMonth))
        }
    }

    fun getDiaryByDate(date: CalendarDay) {
        viewModelScope.launch {
            _diariesByDate.postValue(repository.getDiaryByDate(date.toDateString()))
        }
    }
}