package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.AccessTokenBody
import com.mongmong.namo.domain.model.AuthResponse
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    /** 로그인 */
    // SDK 카카오 로그인
    @POST("auths/kakao/signup")
    suspend fun postKakaoSDK(
        @Body body: AccessTokenBody
    ): LoginResponse

    // SDK 네이버 로그인
    @POST("auths/naver/signup")
    suspend fun postNaverSDK(
        @Body body: AccessTokenBody
    ): LoginResponse

    // 토큰 재발급
    @POST("auths/reissuance")
    suspend fun refreshToken(
        @Body body: TokenBody
    ): RefreshResponse

    // 로그아웃
    @POST("auths/logout")
    suspend fun postLogout(
        @Body body: LogoutBody
    ): AuthResponse

    /** 회원탈퇴 */
    // 카카오 회원탈퇴
    @POST("auths/kakao/delete")
    suspend fun postKakaoQuit(
        @Body body: AccessTokenBody
    ): AuthResponse

    // 네이버 회원탈퇴
    @POST("auths/naver/delete")
    suspend fun postNaverQuit(
        @Body body: AccessTokenBody
    ): AuthResponse
}