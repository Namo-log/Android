package com.example.namo.data.entity.diary

import com.example.namo.R

sealed class DiaryItem {
    abstract val viewType: Int

    data class Header(val date: Long) : DiaryItem() {
        override val viewType: Int = R.layout.item_diary_list
    }

    data class Content(

        var eventId: Long = 0,
        var title: String = "",
        var startLong: Long = 0,
        var endLong: Long = 0,
        var dayInterval: Int = 0,
        var categoryColor: Int = 0,
        var categoryName: String = "",
        var categoryIdx: Int = 0,
        var placeName: String = "없음",
        var placeX: Double = 0.0,
        var placeY: Double = 0.0,
        var placeId: String = "",
        var order: Int = 0,
        var alarmList: List<Int>? = listOf(),
        var hasdiary: Boolean = false,
        var content: String = "",
        var images: List<String>? = null

    ) : DiaryItem() {
        override val viewType: Int = R.layout.item_diary_date_list
    }
}


