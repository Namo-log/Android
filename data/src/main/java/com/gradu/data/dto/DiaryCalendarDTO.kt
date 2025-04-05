package com.gradu.data.dto

import com.mongmong.namo.domain.model.BaseResponse

data class GetCalendarDiaryResponse(
    val result: GetCalendarDiaryResult
): BaseResponse()

data class GetCalendarDiaryResult(
    val year: Int = 1970,
    val month: Int = 1,
    val diaryDateForPersonal: List<String> = emptyList(),
    val diaryDateForMeeting: List<String> = emptyList(),
    val diaryDateForBirthday: List<String> = emptyList()
)

data class GetDiaryByDateResponse(
    val result: List<GetDiaryByDateResult>
): BaseResponse()

data class GetDiaryByDateResult(
    val categoryInfo: CategoryInfo,
    val participantInfo: DiaryArchiveParticipant,
    val scheduleEndDate: String,
    val scheduleStartDate: String,
    val scheduleType: Int,
    val scheduleTitle: String,
    val scheduleId: Long,
    val diaryId: Long
)