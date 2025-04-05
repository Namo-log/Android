package com.gradu.data.datasource.category

import android.util.Log
import com.mongmong.namo.data.dto.CategoryBaseResponse
import com.mongmong.namo.data.dto.CategoryDTO
import com.mongmong.namo.data.dto.CategoryRequestBody
import com.mongmong.namo.data.dto.DeleteCategoryResponse
import com.mongmong.namo.data.dto.GetCategoryResponse
import com.mongmong.namo.data.remote.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteCategoryDataSource @Inject constructor(
    private val apiService: CategoryApiService
) {
    suspend fun getCategories(): List<CategoryDTO> {
        var categoryResponse = GetCategoryResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getCategories()
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "getCategories Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "getCategories Failure $it")
            }
        }
        return categoryResponse.result
    }

    suspend fun addCategory(
        category: CategoryRequestBody,
    ): CategoryBaseResponse {
        var categoryResponse = CategoryBaseResponse(result = "")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.postCategory(category)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "addCategory Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "addCategory Failure $it")
            }
        }
        return categoryResponse
    }

    suspend fun editCategory(
        categoryId: Long,
        category: CategoryRequestBody
    ) : CategoryBaseResponse {
        var categoryResponse = CategoryBaseResponse(result = "")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.patchCategory(categoryId, category)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "editCategory Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "editCategory Failure $it")
            }
        }
        return categoryResponse
    }

    suspend fun deleteCategory(
        categoryId: Long
    ) : DeleteCategoryResponse {
        var categoryResponse = DeleteCategoryResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteCategory(categoryId)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "deleteCategory Success, $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "deleteCategory Fail $it")
            }
        }
        return categoryResponse
    }
}