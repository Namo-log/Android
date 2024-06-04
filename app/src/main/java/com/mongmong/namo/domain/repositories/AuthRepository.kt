package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody

interface AuthRepository {
    /** 로그인 */
    // 카카오
    suspend fun postKakaoLogin(
        accessToken: String
    ): LoginResponse
    // 네이버
    suspend fun postNaverLogin(
        accessToken: String
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
        accessToken: String
    ): Boolean
    // 네이버
    suspend fun postNaverQuit(
        accessToken: String
    ): Boolean
}