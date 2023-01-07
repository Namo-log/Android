package com.example.namo.bottom.home.calendar.events

data class Event(
    var title : String = "",
    var startLong : Long = 0,
    var endLong : Long = 0,
    var interval : Int = 0,
    var color : Int = 0,
    var idx : Int = 0
) {

}
