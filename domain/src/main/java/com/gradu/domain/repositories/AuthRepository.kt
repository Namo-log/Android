package com.gradu.domain.repositories

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.LoginBody
import com.gradu.domain.model.LoginResponse
import com.gradu.domain.model.RefreshResponse
import com.gradu.domain.model.RegisterInfo

interface AuthRepository {
    /** 로그인 */
    suspend fun postLogin(
        loginPlatform: String,
        body: LoginBody
    ): LoginResponse

    /** 회원가입 완료 */
    suspend fun postSignupComplete(
        registerInfo: RegisterInfo
    ): BaseResponse

    /** 토큰 재발급 */
    suspend fun postTokenRefresh(): RefreshResponse

    /** 로그아웃 */
    suspend fun postLogout(): Boolean

    /** 회원탈퇴 */
    suspend fun postQuit(
        loginPlatform: String
    ): Boolean
}