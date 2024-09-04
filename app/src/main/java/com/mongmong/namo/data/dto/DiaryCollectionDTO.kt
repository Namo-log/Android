package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetDiaryCollectionRequest(
    val filterType: String? = null,
    val keyword: String? = null,
    val page: Int = 0
)

data class GetDiaryCollectionResponse(
    val result: List<GetDiaryCollectionResult>
): BaseResponse()

data class GetDiaryCollectionResult(
    val diarySummary: DiarySummary,
    val participantsCount: Int,
    val participantsNames: String?,
    val scheduleDate: String,
    val scheduleId: Long,
    val scheduleType: Int,
    val title: String
)

data class DiarySummary(
    val content: String,
    val diaryId: Long,
    val diaryImages: List<DiaryImage>?
)

data class DiaryImage(
    val diaryImageId: Long,
    val imageUrl: String,
    val orderNumber: Int
)