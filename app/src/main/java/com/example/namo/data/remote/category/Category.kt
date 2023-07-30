package com.example.namo.data.remote.category

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

// 카테고리 생성
class CategoryBody (
    val name : String,
    val paletteId: Int,
    val isShare: Boolean
)

class PostCategoryResponse (
    val result: PostCategoryResult
) : BaseResponse()

class PostCategoryResult (
    @SerializedName("id") val categoryId: Int = 0
)

// 모든 카테고리 조회
class GetCategoryResponse (
    val result: ArrayList<GetCategoryReselt>
) : BaseResponse()

class GetCategoryReselt (
    val catgoryId: Int,
    val name: String,
    val paletteId: Int,
    val isShare: Boolean
)