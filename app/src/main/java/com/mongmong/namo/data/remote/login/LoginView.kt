package com.mongmong.namo.data.remote.login

import com.mongmong.namo.presentation.config.BaseResponse

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

interface LogoutView {
    fun onPostLogoutSuccess(response: BaseResponse)
    fun onPostLogoutFailure(message: String)
}