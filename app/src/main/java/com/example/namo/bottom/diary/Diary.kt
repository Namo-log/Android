package com.example.namo.bottom.diary

import org.joda.time.LocalDateTime

data class Diary(
    var category: String,
    var date:LocalDateTime,
    var title: String,
    var contents: String,
    var rv: MutableList<Gallery>
)

data class Gallery(
    var img: Int
)
