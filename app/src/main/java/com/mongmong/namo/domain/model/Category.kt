package com.mongmong.namo.domain.model

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName

// 카테고리 생성
class PostCategoryResponse (
    val result: PostCategoryResult
) : BaseResponse()

class PostCategoryResult (
    @SerializedName("id") val categoryId: Long = 0
)

// 카테고리 수정
class EditCategoryResponse (
    val result: EditCategoryResult
) : BaseResponse()

class EditCategoryResult (
    @SerializedName("id") val categoryId: Long = 0
)

// 카테고리 삭제
data class DeleteCategoryResponse (
    @SerializedName("result") val result : String
) : BaseResponse()

// 모든 카테고리 조회
class GetCategoryResponse (
    val result: ArrayList<GetCategoryResult>
) : BaseResponse()

class GetCategoryResult (
    val categoryId: Long,
    val name: String,
    val paletteId: Int,
    val isShare: Boolean
)