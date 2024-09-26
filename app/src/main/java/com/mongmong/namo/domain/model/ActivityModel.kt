package com.mongmong.namo.domain.model

import com.mongmong.namo.presentation.config.BaseResponse

data class Activity(
    var endDate: String,
    val activityId: Long,
    var location: ActivityLocation,
    var participants: List<ParticipantInfo>,
    var startDate: String,
    var title: String,
    var tag: String,
    var pay: Int,
    var images: List<DiaryImage>
)


data class ActivityLocation(
    val kakaoLocationId: String = "",
    val latitude: Int = 0,
    val locationName: String = "",
    val longitude: Int = 0
)