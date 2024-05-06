package com.mongmong.namo.data.remote.auth

import android.util.Log
import com.mongmong.namo.data.remote.LoginApiService
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.TokenBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefreshService {
    private lateinit var splashView: SplashView

    fun setSplashView(splashView : SplashView){
        this.splashView=splashView
    }

    private val refreshRetrofitInterface: LoginApiService = ApplicationClass.bRetrofit.create(
        LoginApiService::class.java)
    private val splashRetrofitInterface = ApplicationClass.sRetrofit.create(LoginApiService::class.java)

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