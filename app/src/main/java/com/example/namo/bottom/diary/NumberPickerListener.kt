package com.example.namo.bottom.diary

import android.widget.NumberPicker

interface NumberPickerListener : NumberPicker.OnValueChangeListener, NumberPicker.OnScrollListener {

    var newValue: Int
    var isValueChanged: Boolean

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        isValueChanged = true
        newValue = newVal
        onValueChanged(newVal, false)
    }

    override fun onScrollStateChange(view: NumberPicker?, scrollState: Int) {
        if (isValueChanged && scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
            onValueChanged(newValue, true)
            isValueChanged = false
        }
    }

    fun onValueChanged(value: Int, completed: Boolean)
}