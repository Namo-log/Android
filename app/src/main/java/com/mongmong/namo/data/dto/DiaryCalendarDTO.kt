package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetCalendarDiaryResponse(
    val result: GetCalendarDiaryResult
): BaseResponse()

data class GetCalendarDiaryResult(
    val dates: List<String> = emptyList(),
    val month: Int = 1,
    val year: Int = 1970
)