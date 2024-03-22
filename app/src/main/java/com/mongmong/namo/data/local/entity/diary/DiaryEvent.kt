package com.mongmong.namo.data.local.entity.diary




data class DiarySchedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var startDate: Long = 0,
    var categoryId: Long = 0L,
    var place: String = "없음",
    var content: String?,
    var images: List<String>? = null,
    var serverId: Long = 0L, // eventServerId
    var categoryServerId : Long = 0L,
    var isHeader: Boolean = false
)


