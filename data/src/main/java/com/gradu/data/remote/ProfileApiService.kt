package com.gradu.data.remote

import com.gradu.data.dto.GetProfileResponse
import com.gradu.data.dto.PatchProfileRequest
import com.gradu.domain.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface ProfileApiService {
    @GET("profile")
    suspend fun getProfile() : GetProfileResponse

    @PATCH("profile")
    suspend fun patchProfileInfo(
        @Body profileRequest: PatchProfileRequest
    ): BaseResponse
}