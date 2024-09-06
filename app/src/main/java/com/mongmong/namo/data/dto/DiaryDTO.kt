package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetPersonalDiaryResponse(
    val result: GetPersonalDiaryResult
): BaseResponse()

data class GetPersonalDiaryResult(
    val content: String,
    val diaryId: Long,
    val diaryImages: List<DiaryImage>,
    val enjoyRating: Int
)

data class DiaryImage(
    val diaryImageId: Long,
    val imageUrl: String,
    val orderNumber: Int
)