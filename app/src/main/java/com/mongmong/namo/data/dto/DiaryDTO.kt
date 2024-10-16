package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class DiaryResponse(
    val result: String
) : BaseResponse()

data class GetDiaryResponse(
    val result: GetDiaryResult
): BaseResponse()

data class GetDiaryResult(
    val content: String = "",
    val diaryId: Long = 0L,
    val diaryImages: List<DiaryImage> = emptyList(),
    val enjoyRating: Int = 0
)

data class DiaryImage(
    val diaryImageId: Long,
    val imageUrl: String,
    val orderNumber: Int
)

data class PostDiaryRequest(
    val content: String,
    val diaryImages: List<DiaryRequestImage>,
    val enjoyRating: Int,
    val scheduleId: Long
)

data class DiaryRequestImage(
    val imageUrl: String,
    val orderNumber: Int = 1
)

data class EditDiaryRequest(
    val content: String,
    val diaryImages: List<DiaryRequestImage>,
    val enjoyRating: Int,
    val deleteImages: List<Long>
)

data class GetMoimPaymentResponse(
    val result: GetMoimPaymentResult
): BaseResponse()

data class GetMoimPaymentResult(
    val settlementUserList: List<MoimPaymentParticipant> = emptyList(),
    val totalAmount: Int = 0
)

data class MoimPaymentParticipant(
    val amount: Int,
    val nickname: String
)