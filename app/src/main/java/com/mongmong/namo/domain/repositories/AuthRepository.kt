package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.TokenBody

interface AuthRepository {
    /** 로그인 */
    // 카카오
    suspend fun postKakaoLogin(
        accessToken: String,
        refreshToken: String
    ): LoginResponse

//    suspend fun saveToken(
//        accessToken: String,
//        refreshToken: String
//    )
}