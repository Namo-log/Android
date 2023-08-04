package com.example.namo.data.entity.diary


data class DiaryEvent(
    var eventId: Long = 0L,
    var event_title: String = "",
    var event_start: Long = 0,
    var event_category_idx: Long = 0L,
    var event_place_name: String = "없음",
    var content: String = "",
    var images: List<String>? = null,
    var event_server_idx: Long = 0L,
    )


