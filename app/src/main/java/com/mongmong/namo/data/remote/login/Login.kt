package com.mongmong.namo.data.remote.login

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName

// SDK 카카오 로그인
data class LoginResponse(
    // 베이스 리스폰스를 상속 받았으므로, 아래 내용은 포함이 되었음
//    @SerializedName("code") val code: Int = 0,
//    @SerializedName("message") val message: String = ""
    val result: LoginResult
) : BaseResponse()
data class LoginResult(
    val accessToken: String,
    val refreshToken: String
)

data class TokenBody(
    val accessToken: String,
    val refreshToken: String
)

data class LogoutBody(
    val accessToken: String
)