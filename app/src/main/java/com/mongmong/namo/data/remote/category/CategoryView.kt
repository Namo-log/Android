package com.mongmong.namo.data.remote.category

import com.mongmong.namo.presentation.config.BaseResponse


interface CategoryDetailView {
    // 카테고리 생성
    fun onPostCategorySuccess(response: PostCategoryResponse, categoryId : Long)
    fun onPostCategoryFailure(message: String)

    // 카테고리 수정
    fun onPatchCategorySuccess(response: PostCategoryResponse, categoryId: Long)
    fun onPatchCategoryFailure(message: String)
}

interface  CategorySettingView {
    // 모든 카테고리 조회
    fun onGetAllCategorySuccess(response: GetCategoryResponse)
    fun onGetAllCategoryFailure(message: String)
}

interface  CategoryDeleteView {
    // 카테고리 삭제
    fun onDeleteCategorySuccess(response: BaseResponse, categoryId: Long)
    fun onDeleteCategoryFailure(message: String)
}