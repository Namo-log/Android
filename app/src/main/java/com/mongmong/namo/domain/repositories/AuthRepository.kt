package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse

interface AuthRepository {
    /** 로그인 */
    suspend fun postLogin(
        loginPlatform: String,
        body: LoginBody
    ): LoginResponse

    /** 토큰 재발급 */
    suspend fun postTokenRefresh(): RefreshResponse

    /** 로그아웃 */
    suspend fun postLogout(): Boolean

    /** 회원탈퇴 */
    suspend fun postQuit(
        loginPlatform: String
    ): Boolean
}