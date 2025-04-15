package com.gradu.data.utils.mappers

import com.gradu.data.dto.CategoryDTO
import com.gradu.data.dto.CategoryRequestBody
import com.gradu.domain.model.CategoryModel

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