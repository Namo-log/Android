package com.example.namo.data.entity.home.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var eventId : Int = 0,

    @ColumnInfo(name = "event_title")
    var title : String = "",

    @ColumnInfo(name = "event_start")
    var startLong : Long = 0,

    @ColumnInfo(name = "event_end")
    var endLong : Long = 0,

    @ColumnInfo(name = "event_day_interval")
    var dayInterval : Int = 0,

    @ColumnInfo(name = "event_category_color")
    var categoryColor : Int = 0,

    @ColumnInfo(name = "event_category_name")
    var categoryName : String = "",

    @ColumnInfo(name = "event_category_idx")
    var categoryIdx : Int = 0,

    @ColumnInfo(name = "event_place")
    var place : String = "장소 없음",

    @ColumnInfo(name = "event_order")
    var order : Int = 0
) {

}