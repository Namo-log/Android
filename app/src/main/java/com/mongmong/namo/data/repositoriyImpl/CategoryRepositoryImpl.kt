package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.category.LocalCategoryDataSource
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val localCategoryDataSource: LocalCategoryDataSource,
    private val remoteCategoryDataSource: RemoteCategoryDataSource,
    private val networkChecker: NetworkChecker
) : CategoryRepository {

    override suspend fun addCategory(category: Category) {
        category.categoryId = localCategoryDataSource.addCategory(category) // 로컬에서 카테고리 생성 후 받아온 categoryId로 업데이트
        Log.d("CategoryRepositoryImpl", "addCategory categoryId: ${category.categoryId}\n$category")
        if (networkChecker.isOnline()) {
            val addResponse = remoteCategoryDataSource.addCategoryToServer(category.convertLocalCategoryToServer())
            if (addResponse.code == ScheduleRepositoryImpl.SUCCESS_CODE) {
                Log.d("CategoryRepositoryImpl", "addCategory Success, $addResponse")
                localCategoryDataSource.updateCategoryAfterUpload(
                    localId = category.categoryId,
                    serverId = addResponse.result.categoryId,
                    isUpload = UploadState.IS_UPLOAD.state,
                    status = RoomState.DEFAULT.state,
                )
            } else {
                Log.d(
                    "CategoryRepositoryImpl",
                    "addCategory Fail, code = ${addResponse.code}, message = ${addResponse.message}"
                )
            }
        }
    }

    override suspend fun editCategory(category: Category) {
        Log.d("CategoryRepositoryImpl", "editCategory $category")
        localCategoryDataSource.editCategory(category)
        if (networkChecker.isOnline()) {
            val editResponse = remoteCategoryDataSource.editCategoryToServer(
                category.serverId,
                category.convertLocalCategoryToServer()
            )
            if (editResponse.code == ScheduleRepositoryImpl.SUCCESS_CODE) {
                Log.d("CategoryRepositoryImpl", "editCategory Success, $editResponse")
                updateCategoryAfterUpload(category.categoryId, category.serverId, UploadState.IS_UPLOAD.state, RoomState.DEFAULT.state)
            } else {
                Log.d("CategoryRepositoryImpl", "editCategory Fail, code = ${editResponse.code}, message = ${editResponse.message}")
            }
        }
    }

    override suspend fun updateCategoryAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        status: String
    ) {
        localCategoryDataSource.updateCategoryAfterUpload(localId, serverId, isUpload, status)
    }
}