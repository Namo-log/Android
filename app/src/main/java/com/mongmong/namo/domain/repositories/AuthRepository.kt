package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.TokenBody

interface AuthRepository {
    /** 로그인 */
    // 카카오
    suspend fun postKakaoLogin(
        accessToken: String
    ): LoginResponse

    suspend fun postNaverLogin(
        accessToken: String
    ): LoginResponse

    /** 로그아웃 */
    suspend fun postLogout(
        accessToken: String
    ): Boolean
}