package com.gradu.domain.repositories

import com.gradu.domain.model.CategoryModel

interface CategoryRepository {
    suspend fun getCategories(): List<CategoryModel>

    suspend fun findCategoryById(categoryId: Long): CategoryModel

    suspend fun addCategory(
        category: CategoryModel
    ): Boolean

    suspend fun editCategory(
        category: CategoryModel
    ): Boolean

    suspend fun deleteCategory(
        category: CategoryModel
    ): Boolean
}