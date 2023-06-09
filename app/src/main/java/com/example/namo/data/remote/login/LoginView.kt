package com.example.namo.data.remote.login

interface LoginView {
    fun onPostKakaoSDKSuccess(response: LoginResponse)
    fun onPostKakaoSDKFailure(message: String)
}
interface SplashView {
    fun onVerifyTokenSuccess(response: LoginResponse)
    fun onVerifyTokenFailure(message: String)
}