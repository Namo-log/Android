package com.mongmong.namo.data.remote

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

interface AwsS3ApiService {
    @PUT
    suspend fun uploadImageToS3(
        @Url preSignedUrl: String,
        @Body file: RequestBody
    ): Response<Void>
}