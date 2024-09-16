package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.presentation.config.Constants.SUCCESS_CODE
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteCategoryDataSource: RemoteCategoryDataSource,
    private val networkChecker: NetworkChecker
) : CategoryRepository {

    override suspend fun getCategories(): List<Category> {
        return remoteCategoryDataSource.getCategories().map {
            it.convertToCategory()
        }
    }

    override suspend fun findCategoryById(categoryId: Long): Category {
        val categoryList = getCategories()
        return categoryList.find {
            it.categoryId == categoryId
        } ?: Category()
    }

    override suspend fun addCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "addCategory categoryId: ${category.categoryId}\n$category")
        return remoteCategoryDataSource.addCategoryToServer(category.convertLocalCategoryToServer()).code == SUCCESS_CODE
    }

    override suspend fun editCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "editCategory $category")
        return remoteCategoryDataSource.editCategoryToServer(
            category.categoryId,
            category.convertLocalCategoryToServer()
        ).code == SUCCESS_CODE
    }

    override suspend fun deleteCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "deleteCategory $category")
        return remoteCategoryDataSource.deleteCategoryToServer(
            category.categoryId
        ).code == SUCCESS_CODE
    }
}