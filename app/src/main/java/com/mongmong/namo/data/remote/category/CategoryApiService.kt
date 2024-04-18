package com.mongmong.namo.data.remote.category

import com.mongmong.namo.domain.model.CategoryRequestBody
import com.mongmong.namo.domain.model.DeleteCategoryResponse
import com.mongmong.namo.domain.model.EditCategoryResponse
import com.mongmong.namo.domain.model.GetCategoryResponse
import com.mongmong.namo.domain.model.PostCategoryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CategoryApiService {
    // 카테고리 생성
    @POST("/categories")
    suspend fun postCategory(
        @Body body: CategoryRequestBody
    ) : PostCategoryResponse

    // 카테고리 수정
    @PATCH("/categories/{categoryId}")
    suspend fun patchCategory(
        @Path("categoryId") categoryId: Long,
        @Body body: CategoryRequestBody
    ) : EditCategoryResponse

    // 카테고리 조회
    @GET("/categories")
    fun getCategories() : Call<GetCategoryResponse>

    // 카테고리 삭제
    @DELETE("/categories/{categoryId}")
    suspend fun deleteCategory(
        @Path("categoryId") categoryId: Long
    ) : DeleteCategoryResponse
}