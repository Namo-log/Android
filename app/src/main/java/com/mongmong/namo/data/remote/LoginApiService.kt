package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    // SDK 카카오 로그인
    @POST("auth/kakao/signup")
    suspend fun postKakaoSDK(
        @Body body: TokenBody
    ) : LoginResponse

    // SDK 네이버 로그인
    @POST("auth/naver/signup")
    suspend fun postNaverSDK(
        @Body naverData: TokenBody
    ) : LoginResponse

    // 토큰 재발급
    @POST("auth/reissuance")
    fun refreshToken(
        @Body body: TokenBody
    ) : Call<LoginResponse>

    // 로그아웃
    @POST("auth/logout")
    fun postLogout(
        @Body body: LogoutBody
    ) : Call<BaseResponse>
}