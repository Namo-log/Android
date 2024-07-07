package com.mongmong.namo.data.local.entity.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.presentation.config.RoomState
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
    var dayInterval: Int = 0,

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
    var hasDiary: Boolean? = false,

    @ColumnInfo(name = "isMoim")
    var moimSchedule: Boolean = false

) : Serializable {
    fun convertLocalScheduleToServer() : ScheduleRequestBody {
        return ScheduleRequestBody(
            name = this.title,
            startDate = this.startLong,
            endDate = this.endLong,
            interval = this.dayInterval,
            alarmDate = this.alarmList,
            x = this.placeX,
            y = this.placeY,
            locationName = this.placeName,
            categoryId = this.categoryServerId,
        )
    }

    fun getDefaultSchedule() = Schedule()
}