package com.example.namo.data.remote.login

import android.util.Log
import com.example.namo.config.ApplicationClass
import com.example.namo.config.BaseResponse
import com.example.namo.data.remote.event.EventRetrofitInterface
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
    private val splashRetrofitInterface = ApplicationClass.sRetrofit.create(LoginRetrofitInterface::class.java)

    fun tryTokenRefresh(tokenBody: TokenBody) : Response<LoginResponse> {
        return refreshRetrofitInterface.refreshToken(tokenBody).execute()
    }

    fun splashTokenRefresh(tokenBody: TokenBody) {
        splashRetrofitInterface.refreshToken(tokenBody).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                when (response.code()) {
                    200 -> splashView.onVerifyTokenSuccess(response.body() as LoginResponse)
                    else -> splashView.onVerifyTokenFailure("통신 중 200 외 기타 코드")
                }

            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("SplashRefresh", "onFailure")
                splashView.onVerifyTokenFailure(t.message ?: "통신 오류")
            }
        })
    }
}