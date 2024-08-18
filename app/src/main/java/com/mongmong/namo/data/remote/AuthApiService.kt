package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.AuthResponse
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    // 로그아웃
    @POST("auths/logout")
    suspend fun postLogout(): AuthResponse

    // 회원탈퇴
    @POST("auths/delete/{socialType}")
    suspend fun postQuit(
        @Path("socialType") socialPlatform: String
    ): AuthResponse
}