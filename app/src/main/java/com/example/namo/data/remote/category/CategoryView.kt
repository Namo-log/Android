package com.example.namo.data.remote.category


interface CategoryDetailView {
    // 카테고리 생성
    fun onPostCategorySuccess(response: PostCategoryResponse)
    fun onPostCategoryFailure(message: String)
}