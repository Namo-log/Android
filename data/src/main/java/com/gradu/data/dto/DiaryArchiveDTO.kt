package com.gradu.data.dto

import com.gradu.domain.model.BaseResponse

data class GetDiaryArchiveResponse(
    val result: List<GetDiaryArchiveResult>
): BaseResponse()

data class GetDiaryArchiveResult(
    val categoryInfo: CategoryInfo,
    val diarySummary: DiarySummary,
    val scheduleStartDate: String,
    val scheduleEndDate: String,
    val scheduleId: Long,
    val scheduleType: Int,
    val title: String,
    val isHeader: Boolean = false,
    val participantInfo: DiaryArchiveParticipant
)

data class CategoryInfo(
    val name: String,
    val colorId: Int
)

data class DiaryArchiveParticipant(
    val participantsCount: Int,
    val participantsNames: String?,
)

data class DiarySummary(
    val content: String,
    val diaryId: Long,
    val diaryImages: List<DiaryImage>?
)