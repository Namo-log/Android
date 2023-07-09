package com.example.namo.data.remote.category

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CategoryRetrofitInterface {
    // 카테고리 생성
    @POST("/categories")
    fun postCategory(
        @Body body: CategoryBody
    ) : Call<PostCategoryResponse>
}