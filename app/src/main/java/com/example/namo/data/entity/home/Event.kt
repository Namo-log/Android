package com.example.namo.data.entity.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.namo.R
import java.io.Serializable
import java.lang.Boolean.FALSE

@Entity(tableName = "calendar_event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
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
    var alarmList : List<Int>? = listOf(),

    @ColumnInfo(name = "event_upload")
    var isUpload : Int = 0,

    @ColumnInfo(name = "event_state")
    var state : String = R.string.event_current_default.toString(),

    @ColumnInfo(name = "event_server_idx")
    var serverIdx : Int = 0,

    @ColumnInfo(name = "has_diary")
    var hasDiary: Int = 0

) : Serializable


data class EventForUpload(
    var eventId : Long = 0,
    var name : String = "",
    var startDate : Long = 0,
    var endDate : Long = 0,
    var dayInterval : Int = 0,
    var categoryColor : Int = 0,
    var categoryName : String = "",
    var categoryId : Int = 0,
    var placeName : String = "없음",
    var placeX : Double = 0.0,
    var placeY : Double = 0.0,
    var alarmList : List<Int>? = listOf()
)