package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>

    suspend fun findCategoryById(categoryId: Long): Category

    suspend fun addCategory(
        category: Category
    ): Boolean

    suspend fun editCategory(
        category: Category
    ): Boolean

    suspend fun deleteCategory(
        category: Category
    ): Boolean
}