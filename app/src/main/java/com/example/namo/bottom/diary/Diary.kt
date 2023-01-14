package com.example.namo.bottom.diary

data class Diary(
    var category: String,
    var date:Long=0,
    var title: String,
    var contents: String,
    var rv: MutableList<Gallery>
)

data class Gallery(
    var img: Int
)
