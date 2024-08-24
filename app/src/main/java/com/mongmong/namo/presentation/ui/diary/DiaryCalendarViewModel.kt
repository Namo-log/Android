package com.mongmong.namo.presentation.ui.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiaryCalendarViewModel @Inject constructor() : ViewModel() {

    private val _isBottomSheetOpened = MutableLiveData(false)
    val isBottomSheetOpened: LiveData<Boolean> get() = _isBottomSheetOpened

    fun toggleBottomSheetState() {
        _isBottomSheetOpened.value = _isBottomSheetOpened.value != true
    }
}