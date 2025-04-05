package com.gradu.data.dto

import com.google.gson.annotations.SerializedName
import com.mongmong.namo.domain.model.BaseResponse

data class CategoryRequestBody(
    val categoryName : String,
    val colorId: Int,
    val isShared: Boolean = true
)

data class CategoryBaseResponse(
    val result: String
) : BaseResponse()

/** 카테고리 삭제 */
data class DeleteCategoryResponse (
    @SerializedName("result") val result : String
) : BaseResponse()

/** 모든 카테고리 조회 */
class GetCategoryResponse (
    val result: List<CategoryDTO>
) : BaseResponse()

class CategoryDTO (
    val categoryId: Long,
    val categoryName: String,
    val colorId: Int,
    val baseCategory: Boolean,
    val shared: Boolean
)