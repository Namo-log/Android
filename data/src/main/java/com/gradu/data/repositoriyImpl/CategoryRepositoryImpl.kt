package com.gradu.data.repositoriyImpl

import android.util.Log
import com.gradu.core.config.Constants.SUCCESS_CODE
import com.gradu.data.datasource.category.RemoteCategoryDataSource
import com.gradu.data.remote.NetworkChecker
import com.gradu.domain.model.CategoryModel
import com.gradu.domain.repositories.CategoryRepository
import com.gradu.data.utils.mappers.CategoryMapper.toDTO
import com.gradu.data.utils.mappers.CategoryMapper.toModel
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val remoteCategoryDataSource: RemoteCategoryDataSource,
    private val networkChecker: NetworkChecker
) : CategoryRepository {

    override suspend fun getCategories(): List<CategoryModel> {
        return remoteCategoryDataSource.getCategories().map {
            it.toModel()
        }
    }

    override suspend fun findCategoryById(categoryId: Long): CategoryModel {
        val categoryList = getCategories()
        return categoryList.find {
            it.categoryId == categoryId
        } ?: CategoryModel()
    }

    override suspend fun addCategory(category: CategoryModel): Boolean {
        Log.d("CategoryRepositoryImpl", "addCategory categoryId: ${category.categoryId}\n$category")
        return remoteCategoryDataSource.addCategory(category.toDTO()).code == SUCCESS_CODE
    }

    override suspend fun editCategory(category: CategoryModel): Boolean {
        Log.d("CategoryRepositoryImpl", "editCategory $category")
        return remoteCategoryDataSource.editCategory(
            category.categoryId,
            category.toDTO()
        ).code == SUCCESS_CODE
    }

    override suspend fun deleteCategory(category: CategoryModel): Boolean {
        Log.d("CategoryRepositoryImpl", "deleteCategory $category")
        return remoteCategoryDataSource.deleteCategory(
            category.categoryId
        ).code == SUCCESS_CODE
    }
}