package com.example.namo.data.remote.login

interface LoginView {
    fun onPostKakaoSDKSuccess(response: KakaoSDKResponse)
    fun onPostKakaoSDKFailure(message: String)
}