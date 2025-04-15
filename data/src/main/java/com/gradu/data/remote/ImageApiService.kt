package com.gradu.data.remote

import com.gradu.data.dto.GetPreSignedUrlResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApiService {

    @GET("s3/generate-presigned-url")
    suspend fun getPreSignedUrl(
        @Query("prefix") prefix: String,
        @Query("fileName") fileName: String
    ): GetPreSignedUrlResponse
}