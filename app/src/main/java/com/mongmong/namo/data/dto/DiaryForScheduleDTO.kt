package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetScheduleForDiaryResponse(
    val result: GetScheduleForDiaryResult
): BaseResponse()

data class GetScheduleForDiaryResult(
    val locationInfo: LocationInfo = LocationInfo(),
    val scheduleId: Long = 0L,
    val scheduleStartDate: String = "",
    val scheduleTitle: String = "",
    val categoryInfo: CategoryInfo = CategoryInfo("", 0),
    val participantCount: Int = 0,
    val participantInfo: List<ScheduleForDiaryParticipant> = emptyList(),
    val hasDiary: Boolean = true
)

data class ScheduleForDiaryParticipant(
    val userId: Long = 0L,
    val nickname: String = "",
    val isGuest: Boolean = false
)

data class LocationInfo(
    val kakaoLocationId: String = "",
    val locationName: String = ""
)
