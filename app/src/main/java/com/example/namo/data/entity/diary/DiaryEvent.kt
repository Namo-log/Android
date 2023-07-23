package com.example.namo.data.entity.diary

import com.example.namo.R
import java.lang.Boolean.FALSE

data class DiaryEvent(
    var eventId: Long = 0,
    var event_title: String = "",
    var event_start: Long = 0,
    var event_category_idx: Int = 0,
    var event_category_color: Int = 0,
    var event_place_name: String = "없음",
    var has_diary: Int = 0,
    var content: String = "",
    var images: List<String>? = null,
    var event_upload: Int = 0,
    var event_state: String = R.string.event_current_default.toString(),
    var event_server_idx: Int = 0,

    )


