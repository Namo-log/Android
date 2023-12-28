package com.mongmong.namo.ui.bottom.home.schedule.map.data

data class Place(
    var id : String = "",
    var place_name : String = "없음",
    var address_name : String = "",
    var road_address_name : String = "",
    var x : Double = 0.0,
    var y : Double = 0.0
)

data class ResultSearchPlace(
    var documents : List<Place>
)
