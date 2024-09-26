package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetScheduleForDiaryResponse(
    val result: GetScheduleForDiaryResult
): BaseResponse()

data class GetScheduleForDiaryResult(
    val locationInfo: LocationInfo,
    val scheduleId: Long,
    val scheduleStartDate: String,
    val scheduleTitle: String,
    val categoryInfo: CategoryInfo,
    val participantCount: Int,
    val participantInfo: List<ScheduleForDiaryParticipant>,
    val hasDiary: Boolean
)

data class ScheduleForDiaryParticipant(
    val userId: Long,
    val nickname: String,
    val isGuest: Boolean
)

data class LocationInfo(
    val kakaoLocationId: String,
    val locationName: String
)
