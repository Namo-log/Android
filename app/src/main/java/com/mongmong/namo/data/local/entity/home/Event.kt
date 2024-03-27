package com.mongmong.namo.data.local.entity.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import java.io.Serializable

@Entity(tableName = "schedule_table")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var scheduleId: Long = 0L,

    @ColumnInfo(name = "title")
    var title: String = "",

    @ColumnInfo(name = "startDate")
    var startLong: Long = 0,

    @ColumnInfo(name = "endDate")
    var endLong: Long = 0,

    @ColumnInfo(name = "dayInterval")
    var endDate: Int = 0,

    @ColumnInfo(name = "categoryId")
    var categoryId: Long = 0L,

    @ColumnInfo(name = "place")
    var placeName: String = "없음",

    @ColumnInfo(name = "placeX")
    var placeX: Double = 0.0,

    @ColumnInfo(name = "placeY")
    var placeY: Double = 0.0,

    @ColumnInfo(name = "order")
    var order: Int = 0,

    @ColumnInfo(name = "alarmList")
    var alarmList: List<Int>? = listOf(),

    @ColumnInfo(name = "isUpload")
    var isUpload: Boolean = false,

    @ColumnInfo(name = "state")
    var state: String = RoomState.DEFAULT.state,

    @ColumnInfo(name = "serverId")
    var serverId: Long = 0L,

    @ColumnInfo(name = "categoryServerId")
    var categoryServerId: Long = 0L,

    @ColumnInfo(name = "hasDiary")
    var hasDiary: Int = 0,

    @ColumnInfo(name = "isMoim")
    var moimSchedule: Boolean = false

) : Serializable {
    fun eventToScheduleForUpload() : ScheduleForUpload {
        return ScheduleForUpload(
            name = this.title,
            startDate = this.startLong,
            endDate = this.endLong,
            interval = this.endDate,
            alarmDate = this.alarmList,
            x = this.placeX,
            y = this.placeY,
            locationName = this.placeName,
            categoryId = this.categoryServerId,
        )
    }
}


data class ScheduleForUpload(
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