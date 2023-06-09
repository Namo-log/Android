package com.example.namo.data.remote.login

import android.util.Log
import com.example.namo.config.ApplicationClass
import com.example.namo.config.BaseResponse
import com.kakao.sdk.auth.model.AccessTokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefreshService {
    private lateinit var splashView: SplashView

    fun setSplashView(splashView : SplashView){
        this.splashView=splashView
    }

    private val refreshRetrofitInterface: LoginRetrofitInterface = ApplicationClass.bRetrofit.create(LoginRetrofitInterface::class.java)

    fun tryTokenRefresh(tokenBody: TokenBody) : Response<LoginResponse> {
        return refreshRetrofitInterface.refreshToken(tokenBody).execute()
    }
}