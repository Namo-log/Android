package com.mongmong.namo.data.remote

import com.mongmong.namo.data.dto.GetPreSignedUrlResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageApiService {

    @GET("s3/generate-presigned-url")
    suspend fun getPreSignedUrl(
        @Query("prefix") prefix: String,
        @Query("fileName") fileName: String
    ): GetPreSignedUrlResponse
}