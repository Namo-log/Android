package com.mongmong.namo.presentation.utils

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Rect
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager

fun Activity.hideKeyboardOnTouchOutside(ev: MotionEvent?) {
    val focusView = currentFocus
    if (focusView != null && ev != null) {
        val rect = Rect()
        focusView.getGlobalVisibleRect(rect)
        val x = ev.x.toInt()
        val y = ev.y.toInt()

        if (!rect.contains(x, y)) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focusView.windowToken, 0)
            focusView.clearFocus()
        }
    }
}
