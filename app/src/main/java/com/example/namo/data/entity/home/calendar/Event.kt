package com.example.namo.data.entity.home.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "homeCalendarEvent")
data class Event(
    @PrimaryKey(autoGenerate = true)
    var eventId : Int = 0,

    @ColumnInfo(name = "event_title")
    var title : String = "",

    var startLong : Long = 0,
    var endLong : Long = 0,
    var interval : Int = 0,
    var color : Int = 0,
    var idx : Int = 0
) {

}
