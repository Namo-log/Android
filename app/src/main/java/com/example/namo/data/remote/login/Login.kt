package com.example.namo.data.remote.login

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

// SDK 카카오 로그인
data class KakaoSDKResponse(
    // 베이스 리스폰스를 상속 받았으므로, 아래 내용은 포함이 되었음
//    @SerializedName("code") val code: Int = 0,
//    @SerializedName("message") val message: String = ""
    val result: KakaoSDKResult
) : BaseResponse()
data class KakaoSDKResult(
    val userId: Int,
    val isNewUser: Boolean,
    val nickname: String,
    val accessToken: String,
    val refreshToken: String
)
data class SdkTokenBody(
    val kakaoAccessToken: String,
    val kakaoRefreshToken: String
)