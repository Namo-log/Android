package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>

    suspend fun addCategory(
        category: Category
    )

    suspend fun editCategory(
        category: Category
    )

    suspend fun deleteCategory(
        category: Category
    )

    suspend fun updateCategoryAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        status: String
    )
}