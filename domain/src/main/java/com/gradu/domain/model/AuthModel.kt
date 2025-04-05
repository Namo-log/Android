package com.gradu.domain.model

import android.net.Uri

data class AuthResponse(
    val result: String
) : BaseResponse() // 기본 string

// SDK 로그인
data class LoginResponse(
    // 베이스 리스폰스를 상속 받았으므로, 아래 내용은 포함이 되었음
//    @SerializedName("code") val code: Int = 0,
//    @SerializedName("message") val message: String = ""
    val result: LoginResult
) : BaseResponse()

data class LoginResult(
    val accessToken: String = "",
    val refreshToken: String = "",
    val userId: Long = 0L,
    val newUser: Boolean = false,
    val signUpComplete: Boolean = true,
    val terms: List<TermsResult> = emptyList(),
    var userName: String = ""
)

data class TermsResult(
    val content: String,
    val check: Boolean
)

// 토큰 재발급
data class RefreshResponse(
    val result: RefreshResult
) : BaseResponse()

data class RefreshResult(
    val accessToken: String,
    val refreshToken: String
)

data class TokenBody(
    val accessToken: String,
    val refreshToken: String
)

data class LoginBody(
    val accessToken: String,
    val socialRefreshToken: String
)

// 회원가입
data class RegisterInfo(
    val intro: String,
    val birthday: String,
    val colorId: Int,
    val name: String,
    val nickname: String,
    val profileImage: String
)