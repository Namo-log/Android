package com.gradu.domain.model

import java.io.Serializable

data class CategoryModel(
    var categoryId: Long = 0,
    var name: String = "",
    var colorId: Int = 0,
    var basicCategory: Boolean = false, // 기본 카테고리는 삭제 불가
    var isShare: Boolean = false,
) : Serializable {

    fun convertCategoryToScheduleCategory() : ScheduleCategoryInfo {
        return ScheduleCategoryInfo(
            categoryId = this.categoryId,
            colorId = this.colorId,
            name = this.name
        )
    }
}