package com.example.namo.data.entity.diary

import androidx.room.ColumnInfo
import java.lang.Boolean.FALSE

data class DiaryEvent(
    @ColumnInfo(name = "eventId")
    var eventId: Long = 0,
    @ColumnInfo(name = "event_title")
    var title: String = "",
    @ColumnInfo(name = "event_start")
    var startLong: Long = 0,
    @ColumnInfo(name = "event_end")
    var endLong: Long = 0,
    @ColumnInfo(name = "event_day_interval")
    var dayInterval: Int = 0,
    @ColumnInfo(name = "event_category_color")
    var categoryColor: Int = 0,
    @ColumnInfo(name = "event_category_name")
    var categoryName: String = "",
    @ColumnInfo(name = "event_category_idx")
    var categoryIdx: Int = 0,
    @ColumnInfo(name = "event_place_name")
    var placeName: String = "없음",
    @ColumnInfo(name = "event_place_x")
    var placeX: Double = 0.0,
    @ColumnInfo(name = "event_place_y")
    var placeY: Double = 0.0,
    @ColumnInfo(name = "event_place_id")
    var placeId: String = "",
    @ColumnInfo(name = "event_order")
    var order: Int = 0,
    @ColumnInfo(name = "alarm_list")
    var alarmList: List<Int>? = listOf(),
    @ColumnInfo(name = "has_diary")
    var hasDiary: Boolean = FALSE,
    val scheduleIdx: Int,
    var content: String = "",
    var images: List<String>? = null
)


