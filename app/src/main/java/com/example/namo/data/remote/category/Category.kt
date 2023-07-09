package com.example.namo.data.remote.category

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

// 카테고리 생성
class CategoryBody (
    val name : String = "",
    val color: Int = 0, // paletteId
    val isShare: Boolean = true
)

class PostCategoryResponse (
    val result: PostCategoryResult
) : BaseResponse()

class PostCategoryResult (
    @SerializedName("id") val categoryIdx: Int = 0
)