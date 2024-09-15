package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import com.mongmong.namo.domain.model.Category

/** 카테고리 생성 */
class PostCategoryResponse (
    val result: PostCategoryResult
) : BaseResponse()

class PostCategoryResult (
    @SerializedName("id") val categoryId: Long = 0
)

data class CategoryRequestBody(
    val name : String,
    val paletteId: Int,
    val isShare: Boolean = true
)

/** 카테고리 수정 */
class EditCategoryResponse (
    val result: EditCategoryResult
) : BaseResponse()

class EditCategoryResult (
    @SerializedName("id") val categoryId: Long = 0
)

/** 카테고리 삭제 */
data class DeleteCategoryResponse (
    @SerializedName("result") val result : String
) : BaseResponse()

/** 모든 카테고리 조회 */
class GetCategoryResponse (
    val result: List<GetCategoryResult>
) : BaseResponse()

class GetCategoryResult (
    val categoryId: Long,
    val categoryName: String,
    val colorId: Int,
    val baseCategory: Boolean,
    val shared: Boolean
) {
    fun convertToCategory(): Category {
        return Category(
            categoryId = this.categoryId,
            name = this.categoryName,
            colorId = this.colorId,
            isShare = this.shared,
        )
    }
}