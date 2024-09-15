package com.mongmong.namo.presentation.ui.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.CalendarDay
import com.mongmong.namo.domain.model.CalendarDiaryDate
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
    private val _isBottomSheetOpened = MutableLiveData(false)
    val isBottomSheetOpened: LiveData<Boolean> = _isBottomSheetOpened

    private val _selectedDate = MutableLiveData<CalendarDay>()
    val selectedDate: LiveData<CalendarDay> = _selectedDate

    private val _calendarDiaryResult = MutableLiveData<CalendarDiaryDate>()
    val calendarDiaryResult: LiveData<CalendarDiaryDate> = _calendarDiaryResult

    fun toggleBottomSheetState() {
        _isBottomSheetOpened.value = _isBottomSheetOpened.value != true
    }

    fun setSelectedDate(date: CalendarDay) {
        _selectedDate.value = date
    }

    fun getDateFormatted(): String {
        val calendar = Calendar.getInstance().apply {
            selectedDate.value?.let { set(it.year, it.month, it.date.toInt()) }
        }
        val dateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    fun getCalendarDiary(yearMonth: String) {
        viewModelScope.launch {
            _calendarDiaryResult.postValue(repository.getCalendarDiary(yearMonth))
        }
    }
}