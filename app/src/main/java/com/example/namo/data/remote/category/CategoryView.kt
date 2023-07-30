package com.example.namo.data.remote.category


interface CategoryDetailView {
    // 카테고리 생성
    fun onPostCategorySuccess(response: PostCategoryResponse)
    fun onPostCategoryFailure(message: String)

    // 카테고리 수정
    fun onPatchCategorySuccess(response: PostCategoryResponse)
    fun onPatchCategoryFailure(message: String)
}

interface  CategorySettingView {
    // 모든 카테고리 조회
    fun onGetAllCategorySuccess(response: GetCategoryResponse)
    fun onGetAllCategoryFailure(message: String)
}