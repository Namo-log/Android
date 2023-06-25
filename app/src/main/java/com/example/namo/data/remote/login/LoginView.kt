package com.example.namo.data.remote.login

interface LoginView {
    // Kakao
    fun onPostKakaoSDKSuccess(response: LoginResponse)
    fun onPostKakaoSDKFailure(message: String)
    // Naver
    fun onPostNaverSDKSuccess(response: LoginResponse)
    fun onPostNaverSDKFailure(message: String)
}
interface SplashView {
    fun onVerifyTokenSuccess(response: LoginResponse)
    fun onVerifyTokenFailure(message: String)
}