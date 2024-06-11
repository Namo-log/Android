package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse

interface AuthRepository {
    /** 로그인 */
    // 카카오
    suspend fun postKakaoLogin(
        body: LoginBody
    ): LoginResponse
    // 네이버
    suspend fun postNaverLogin(
        body: LoginBody
    ): LoginResponse

    /** 토큰 재발급 */
    suspend fun postTokenRefresh(
        accessToken: String,
        refreshToken: String
    ): RefreshResponse

    /** 로그아웃 */
    suspend fun postLogout(
        accessToken: String
    ): Boolean

    /** 회원탈퇴 */
    // 카카오
    suspend fun postKakaoQuit(
        bearerToken: String
    ): Boolean
    // 네이버
    suspend fun postNaverQuit(
        bearerToken: String
    ): Boolean
}