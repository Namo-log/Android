package com.example.namo.bottom.diary

data class DiaryDummy(
    var category: String,
    var year:Int,
    var month:Int,
    var date:Int,
    var title: String,
    var contents: String,
    var rv: MutableList<GalleryDummy>
)

data class GalleryDummy(
    var img: Int
)
