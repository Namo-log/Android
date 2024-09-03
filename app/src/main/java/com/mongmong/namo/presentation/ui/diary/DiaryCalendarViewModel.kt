package com.mongmong.namo.presentation.ui.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.presentation.ui.diary.adapter.CalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiaryCalendarViewModel @Inject constructor() : ViewModel() {

    private val _isBottomSheetOpened = MutableLiveData(false)
    val isBottomSheetOpened: LiveData<Boolean> = _isBottomSheetOpened

    private val _selectedDate = MutableLiveData<CalendarDay>()
    val selectedDate: LiveData<CalendarDay> = _selectedDate

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
}