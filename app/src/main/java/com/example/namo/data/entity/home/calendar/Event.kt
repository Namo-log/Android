package com.example.namo.data.entity.home.calendar

import androidx.room.Entity

@Entity(tableName = "homeCalendarEvent")
data class Event(
    var title : String = "",
    var startLong : Long = 0,
    var endLong : Long = 0,
    var interval : Int = 0,
    var color : Int = 0,
    var idx : Int = 0
) {

}
