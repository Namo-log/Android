package com.gradu.data.remote

import com.mongmong.namo.data.dto.GetProfileResponse
import com.mongmong.namo.data.dto.PatchProfileRequest
import com.mongmong.namo.data.dto.SignupCompleteResponse
import com.mongmong.namo.domain.model.BaseResponse
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