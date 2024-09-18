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

data class GetDiaryByDateResponse(
    val result: List<GetDiaryByDateResult>
): BaseResponse()

data class GetDiaryByDateResult(
    val categoryInfo: CategoryInfo,
    val participantInfo: ParticipantInfo,
    val scheduleEndDate: String,
    val scheduleStartDate: String,
    val scheduleType: Int,
    val scheduleTitle: String,
    val scheduleId: Long,
    val diaryId: Long
)