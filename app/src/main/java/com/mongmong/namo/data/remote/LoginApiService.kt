package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.AccessTokenBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiService {
    // SDK 카카오 로그인
    @POST("auths/kakao/signup")
    suspend fun postKakaoSDK(
        @Body body: AccessTokenBody
    ) : LoginResponse

    // SDK 네이버 로그인
    @POST("auths/naver/signup")
    suspend fun postNaverSDK(
        @Body body: AccessTokenBody
    ) : LoginResponse

    // 토큰 재발급
    @POST("auths/reissuance")
    fun refreshToken(
        @Body body: TokenBody
    ) : Call<LoginResponse>

    // 로그아웃
    @POST("auths/logout")
    fun postLogout(
        @Body body: LogoutBody
    ) : Call<BaseResponse>
}