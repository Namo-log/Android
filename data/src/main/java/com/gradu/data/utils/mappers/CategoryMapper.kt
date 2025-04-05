package com.gradu.data.utils.mappers

import com.mongmong.namo.data.dto.CategoryDTO
import com.mongmong.namo.data.dto.CategoryRequestBody
import com.mongmong.namo.domain.model.CategoryModel

object CategoryMapper {
    fun CategoryModel.toDTO(): CategoryRequestBody {
        return CategoryRequestBody(
            categoryName = this.name,
            colorId = this.colorId,
            isShared = this.isShare
        )
    }

    fun CategoryDTO.toModel(): CategoryModel {
        return CategoryModel(
            categoryId = this.categoryId,
            name = this.categoryName,
            colorId = this.colorId,
            isShare = this.shared,
            basicCategory = this.baseCategory
        )
    }
}