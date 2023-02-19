package com.example.namo.ui.bottom.home.schedule.map.data

data class Place(
    var id : String = "",
    var place_name : String = "없음",
    var address_name : String = "",
    var road_address_name : String = "",
    var x : String = "",
    var y : String = ""
)

data class ResultSearchPlace(
    var documents : List<Place>
)
