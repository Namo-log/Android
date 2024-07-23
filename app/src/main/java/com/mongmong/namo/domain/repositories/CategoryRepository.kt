package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>

    suspend fun findCategoryById(localId: Long, serverId: Long): Category

    suspend fun addCategory(
        category: Category
    ): Boolean

    suspend fun editCategory(
        category: Category
    ): Boolean

    suspend fun deleteCategory(
        category: Category
    ): Boolean

    suspend fun updateCategoryAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        status: String
    )
}