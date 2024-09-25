package com.mongmong.namo.domain.model

import com.mongmong.namo.data.dto.CalendarColorInfo
import com.mongmong.namo.data.dto.CategoryRequestBody
import com.mongmong.namo.data.dto.ScheduleCategoryInfo
import java.io.Serializable

data class Category(
    var categoryId: Long = 0,
    var name: String = "",
    var colorId: Int = 0,
    var basicCategory: Boolean = false,
    var isShare: Boolean = false,
) : Serializable {
    fun convertLocalCategoryToServer() : CategoryRequestBody {
        return CategoryRequestBody(
            name = this.name,
            paletteId = this.colorId,
            isShare = this.isShare
        )
    }

    fun convertCategoryToScheduleCategory() : ScheduleCategoryInfo {
        return ScheduleCategoryInfo(
            categoryId = this.categoryId,
            colorId = this.colorId,
            name = this.name,
            isShare = this.isShare
        )
    }

    fun getCategoryColorInfo(): CalendarColorInfo {
        return CalendarColorInfo(this.colorId, this.name)
    }
}