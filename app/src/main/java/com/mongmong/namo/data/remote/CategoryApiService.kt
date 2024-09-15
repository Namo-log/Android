package com.mongmong.namo.data.remote

import com.mongmong.namo.data.dto.CategoryRequestBody
import com.mongmong.namo.data.dto.DeleteCategoryResponse
import com.mongmong.namo.data.dto.EditCategoryResponse
import com.mongmong.namo.data.dto.GetCategoryResponse
import com.mongmong.namo.data.dto.PostCategoryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoryApiService {
    // 카테고리 조회
    @GET("categories")
    suspend fun getCategories() : GetCategoryResponse

    // 카테고리 생성
    @POST("categories")
    suspend fun postCategory(
        @Body body: CategoryRequestBody
    ) : PostCategoryResponse

    // 카테고리 수정
    @PATCH("categories/{categoryId}")
    suspend fun patchCategory(
        @Path("categoryId") categoryId: Long,
        @Body body: CategoryRequestBody
    ) : EditCategoryResponse

    // 카테고리 삭제
    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("categoryId") categoryId: Long
    ) : DeleteCategoryResponse
}