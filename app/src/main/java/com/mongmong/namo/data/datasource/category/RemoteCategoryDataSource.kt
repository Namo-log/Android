package com.mongmong.namo.data.datasource.category

import android.util.Log
import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.domain.model.CategoryRequestBody
import com.mongmong.namo.domain.model.DeleteCategoryResponse
import com.mongmong.namo.domain.model.EditCategoryResponse
import com.mongmong.namo.domain.model.EditCategoryResult
import com.mongmong.namo.domain.model.GetCategoryResponse
import com.mongmong.namo.domain.model.GetCategoryResult
import com.mongmong.namo.domain.model.PostCategoryResponse
import com.mongmong.namo.domain.model.PostCategoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteCategoryDataSource @Inject constructor(
    private val apiService: CategoryApiService
) {
    suspend fun getCategories(): List<GetCategoryResult> {
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

    suspend fun addCategoryToServer(
        category: CategoryRequestBody,
    ): PostCategoryResponse {
        var categoryResponse = PostCategoryResponse(result = PostCategoryResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.postCategory(category)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "addCategoryToServer Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "addCategoryToServer Failure")
            }
        }
        return categoryResponse
    }

    suspend fun editCategoryToServer(
        categoryId: Long,
        category: CategoryRequestBody
    ) : EditCategoryResponse {
        var categoryResponse = EditCategoryResponse(result = EditCategoryResult(categoryId))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.patchCategory(categoryId, category)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "editCategoryToServer Success $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "editCategoryToServer Failure")
            }
        }
        return categoryResponse
    }

    suspend fun deleteCategoryToServer(
        categoryId: Long
    ) : DeleteCategoryResponse {
        var categoryResponse = DeleteCategoryResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteCategory(categoryId)
            }.onSuccess {
                Log.d("RemoteCategoryDataSource", "deleteCategoryToServer Success, $it")
                categoryResponse = it
            }.onFailure {
                Log.d("RemoteCategoryDataSource", "deleteCategoryToServer Fail")
            }
        }
        return categoryResponse
    }
}