package com.example.namo.data.remote.category

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryRetrofitInterface {
    // 카테고리 생성
    @POST("/categories")
    fun postCategory(
        @Body body: CategoryBody
    ) : Call<PostCategoryResponse>

    // 카테고리 수정
    @PATCH("/categories/{categoryId}")
    fun patchCategory(
        @Path("categoryId") categoryId: Int,
        @Body body: CategoryBody
    ) : Call<PostCategoryResponse>
}