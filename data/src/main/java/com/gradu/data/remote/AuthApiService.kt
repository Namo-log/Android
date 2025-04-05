package com.gradu.data.remote

import com.mongmong.namo.data.dto.SignupCompleteRequest
import com.mongmong.namo.data.dto.SignupCompleteResponse
import com.mongmong.namo.domain.model.AuthResponse
import com.mongmong.namo.domain.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    // 로그아웃
    @POST("auths/logout")
    suspend fun postLogout(): AuthResponse

    @POST("auths/signup/complete")
    suspend fun postSignupComplete(@Body registerRequest: SignupCompleteRequest): SignupCompleteResponse

    // 회원탈퇴
    @POST("auths/delete/{socialType}")
    suspend fun postQuit(
        @Path("socialType") socialPlatform: String
    ): AuthResponse
}