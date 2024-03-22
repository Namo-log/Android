package com.mongmong.namo.data.local.entity.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.RoomState
import java.io.Serializable

@Entity(tableName = "calendar_event_table")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var eventId: Long = 0L,

    @ColumnInfo(name = "event_title")
    var title: String = "",

    @ColumnInfo(name = "event_start")
    var startLong: Long = 0,

    @ColumnInfo(name = "event_end")
    var endLong: Long = 0,

    @ColumnInfo(name = "event_day_interval")
    var dayInterval: Int = 0,

    @ColumnInfo(name = "event_category_idx")
    var categoryIdx: Long = 0L,

    @ColumnInfo(name = "event_place_name")
    var placeName: String = "없음",

    @ColumnInfo(name = "event_place_x")
    var placeX: Double = 0.0,

    @ColumnInfo(name = "event_place_y")
    var placeY: Double = 0.0,

    @ColumnInfo(name = "event_order")
    var order: Int = 0,

    @ColumnInfo(name = "alarm_list")
    var alarmList: List<Int>? = listOf(),

    @ColumnInfo(name = "event_upload")
    var isUpload: Boolean = false,

    @ColumnInfo(name = "event_state")
    var state: String = RoomState.DEFAULT.state,

    @ColumnInfo(name = "event_server_idx")
    var serverIdx: Long = 0L,

    @ColumnInfo(name = "event_category_server_idx")
    var categoryServerIdx: Long = 0L,

    @ColumnInfo(name = "has_diary")
    var hasDiary: Int = 0,

    @ColumnInfo(name = "event_is_group")
    var moimSchedule: Boolean = false

) : Serializable {
    fun eventToEventForUpload() : EventForUpload {
        return EventForUpload(
            name = this.title,
            startDate = this.startLong,
            endDate = this.endLong,
            interval = this.dayInterval,
            alarmDate = this.alarmList,
            x = this.placeX,
            y = this.placeY,
            locationName = this.placeName,
            categoryId = this.categoryServerIdx,
        )
    }
}


data class EventForUpload(
    var name: String = "",
    var startDate: Long = 0L,
    var endDate: Long = 0L,
    var interval: Int = 0,
    var alarmDate: List<Int>? = listOf(),
    var x: Double = 0.0,
    var y: Double = 0.0,
    var locationName: String = "없음",
    var categoryId: Long = 0L
)