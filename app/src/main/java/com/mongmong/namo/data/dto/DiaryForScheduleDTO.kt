package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetScheduleForDiaryResponse(
    val result: GetScheduleForDiaryResult
): BaseResponse()

data class GetScheduleForDiaryResult(
    val location: Location,
    val scheduleId: Long,
    val scheduleStartDate: String,
    val scheduleTitle: String
)

data class Location(
    val kakaoLocationId: String,
    val locationName: String
)