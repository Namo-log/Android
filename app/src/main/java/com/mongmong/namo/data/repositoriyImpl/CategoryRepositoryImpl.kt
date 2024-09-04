package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.category.LocalCategoryDataSource
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.presentation.config.Constants.SUCCESS_CODE
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val localCategoryDataSource: LocalCategoryDataSource,
    private val remoteCategoryDataSource: RemoteCategoryDataSource,
    private val networkChecker: NetworkChecker
) : CategoryRepository {

    override suspend fun getCategories(): List<Category> {
        return remoteCategoryDataSource.getCategories().map {
            it.convertToCategory()
        }
    }

    override suspend fun findCategoryById(localId: Long, serverId: Long): Category {
        val categoryList = getCategories()
        return categoryList.find {
            if (it.serverId != 0L) it.serverId == serverId
            else it.categoryId == localId
        } ?: Category()
    }

    override suspend fun addCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "addCategory categoryId: ${category.categoryId}\n$category")
        return remoteCategoryDataSource.addCategoryToServer(category.convertLocalCategoryToServer()).code == SUCCESS_CODE
        /*
        category.categoryId = localCategoryDataSource.addCategory(category) // 로컬에서 카테고리 생성 후 받아온 categoryId로 업데이트
        if (networkChecker.isOnline()) {
            val addResponse = remoteCategoryDataSource.addCategoryToServer(category.convertLocalCategoryToServer())
            if (addResponse.code == ScheduleRepositoryImpl.SUCCESS_CODE) {
                Log.d("CategoryRepositoryImpl", "addCategory Success, $addResponse")
                updateCategoryAfterUpload(
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
         */
    }

    override suspend fun editCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "editCategory $category")
        return remoteCategoryDataSource.editCategoryToServer(
            category.serverId,
            category.convertLocalCategoryToServer()
        ).code == SUCCESS_CODE
        /*
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
         */
    }

    override suspend fun deleteCategory(category: Category): Boolean {
        Log.d("CategoryRepositoryImpl", "deleteCategory $category")
        return remoteCategoryDataSource.deleteCategoryToServer(
            category.serverId
        ).code == SUCCESS_CODE
        /*
        // room db에서 삭제 상태로 변경
        localCategoryDataSource.deleteCategory(category)
        if (networkChecker.isOnline()) {
            // 서버 db에서 삭제
            val deleteResponse = remoteCategoryDataSource.deleteCategoryToServer(
                category.serverId
            )
            if (deleteResponse.code == ScheduleRepositoryImpl.SUCCESS_CODE) {
                Log.d("CategoryRepositoryImpl", "deleteCategory Success, $deleteResponse")
                updateCategoryAfterUpload(category.categoryId, category.serverId, UploadState.IS_UPLOAD.state, RoomState.DEFAULT.state)
            } else {
                Log.d("CategoryRepositoryImpl", "deleteCategory Fail, code = ${deleteResponse.code}, message = ${deleteResponse.message}")
            }
        }
         */
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