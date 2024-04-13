package com.mongmong.namo.data.datasource.category

import android.util.Log
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.local.entity.home.CategoryForUpload
import com.mongmong.namo.data.remote.category.CategoryRetrofitInterface
import com.mongmong.namo.domain.model.DeleteCategoryResponse
import com.mongmong.namo.domain.model.EditCategoryResponse
import com.mongmong.namo.domain.model.EditCategoryResult
import com.mongmong.namo.domain.model.PostCategoryResponse
import com.mongmong.namo.domain.model.PostCategoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteCategoryDataSource @Inject constructor(
    private val apiService: CategoryRetrofitInterface
) {
    suspend fun addCategoryToServer(
        category: CategoryForUpload,
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
        category: CategoryForUpload
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