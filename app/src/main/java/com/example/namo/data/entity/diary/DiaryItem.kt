package com.example.namo.data.entity.diary

import android.graphics.Bitmap
import com.example.namo.R

sealed class DiaryItem {
    abstract val viewType: Int

    data class Header(val date: Long) : DiaryItem() {
        override val viewType: Int = R.layout.item_diary_list
    }

    data class Content(
        var eventId: Long,
        var title: String,
        var startLong: Long,
        var place: String,
        var categoryIdx: Int,
        var categoryColor: Int,
        var hasDiary: Boolean,
        var content: String,
        var imgs: List<String>?

    ) : DiaryItem() {
        override val viewType: Int = R.layout.item_diary_date_list
    }
}


