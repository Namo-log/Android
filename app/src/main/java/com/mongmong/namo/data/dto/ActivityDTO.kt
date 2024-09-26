package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetActivitiesResponse(
    val result: List<GetActivitiesResult>
): BaseResponse()

data class GetActivitiesResult(
    val activityEndDate: String,
    val activityId: Int,
    val activityLocation: ActivityLocation,
    val activityParticipants: List<ActivityParticipant>,
    val activityStartDate: String,
    val activityTitle: String,
    val tag: String,
    val totalAmount: Int,
    val activityImages: List<ActivityImage>
)

data class ActivityParticipant(
    val participantMemberId: Int,
    val participantNickname: String
)

data class ActivityLocation(
    val kakaoLocationId: String,
    val latitude: Double,
    val locationName: String,
    val longitude: Double
)

data class ActivityImage(
    val orderNumber: Int,
    val imageUrl: String,
    val activityImageId: Long
)