package com.mongmong.namo.data.datasource.category

import android.util.Log
import com.mongmong.namo.data.local.dao.CategoryDao
import com.mongmong.namo.data.local.entity.home.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalCategoryDataSource @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun addCategory(category: Category): Long {
        var localId = 0L
        withContext(Dispatchers.IO) {
            runCatching {
                categoryDao.insertCategory(category)
            }.onSuccess {
                Log.d("LocalCategoryDataSource", "addCategory Success, categoryId: $it")
                localId = it // 룸디비 일정 추가 결과
            }.onFailure {
                Log.d("LocalCategoryDataSource", "addCategory Fail")
            }
        }
        return localId
    }

    suspend fun editCategory(category: Category) {
        withContext(Dispatchers.IO) {
            runCatching {
                categoryDao.updateCategory(category)
            }.onSuccess {
                Log.d("LocalCategoryDataSource", "editCategory Success")
            }.onFailure {
                Log.d("LocalCategoryDataSource", "editCategory Fail")
            }
        }
    }

    suspend fun updateCategoryAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        status: String
    ) {
        Log.d("LocalCategoryDataSource updateCategoryAfterUpload", "$localId, $serverId")
        categoryDao.updateCategoryAfterUpload(
            localId,
            isUpload,
            serverId,
            status
        )
    }
}