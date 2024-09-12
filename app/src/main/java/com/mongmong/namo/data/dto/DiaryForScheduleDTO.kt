package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetScheduleForDiaryResponse(
    val result: GetScheduleForDiaryResult
): BaseResponse()

data class GetScheduleForDiaryResult(
    val locationInfo: Location,
    val scheduleId: Long,
    val scheduleStartDate: String,
    val scheduleTitle: String,
    val categoryInfo: CategoryInfo,
    val hasDiary: Boolean
)

data class Location(
    val kakaoLocationId: String,
    val locationName: String
)
