package com.mongmong.namo.data.datasource.category

import android.util.Log
import com.mongmong.namo.data.local.entity.home.CategoryForUpload
import com.mongmong.namo.data.remote.category.CategoryRetrofitInterface
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
}