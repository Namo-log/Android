package com.mongmong.namo.data.remote.category

import com.mongmong.namo.domain.model.GetCategoryResponse
import com.mongmong.namo.domain.model.PostCategoryResponse
import com.mongmong.namo.presentation.config.BaseResponse


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