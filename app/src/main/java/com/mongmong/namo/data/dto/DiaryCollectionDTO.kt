package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetDiaryCollectionResponse(
    val result: List<GetDiaryCollectionResult>
): BaseResponse()

data class GetDiaryCollectionResult(
    val categoryInfo: CategoryInfo,
    val diarySummary: DiarySummary,
    val scheduleStartDate: String,
    val scheduleEndDate: String,
    val scheduleId: Long,
    val scheduleType: Int,
    val title: String,
    val isHeader: Boolean = false,
    val participantInfo: DiaryCollectionParticipant
)

data class CategoryInfo(
    val name: String,
    val colorId: Int
)

data class DiaryCollectionParticipant(
    val participantsCount: Int,
    val participantsNames: String?,
)

data class DiarySummary(
    val content: String,
    val diaryId: Long,
    val diaryImages: List<DiaryImage>?
)