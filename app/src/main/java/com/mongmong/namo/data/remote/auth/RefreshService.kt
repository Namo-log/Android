package com.mongmong.namo.data.remote.auth

import android.util.Log
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RefreshService {
    private lateinit var splashView: SplashView

    fun setSplashView(splashView : SplashView){
        this.splashView=splashView
    }

    private val refreshRetrofitInterface: AuthApiService = ApplicationClass.bRetrofit.create(
        AuthApiService::class.java)
    private val splashRetrofitInterface = ApplicationClass.sRetrofit.create(AuthApiService::class.java)

    fun tryTokenRefresh(tokenBody: TokenBody) : Response<RefreshResponse> {
        return refreshRetrofitInterface.refreshToken(tokenBody).execute()
    }

    fun splashTokenRefresh(tokenBody: TokenBody) {
        splashRetrofitInterface.refreshToken(tokenBody).enqueue(object : Callback<RefreshResponse> {

            override fun onResponse(call: Call<RefreshResponse>, response: Response<RefreshResponse>) {
                when (response.code()) {
                    200 -> splashView.onVerifyTokenSuccess(response.body() as RefreshResponse)
                    else -> splashView.onVerifyTokenFailure("통신 중 200 외 기타 코드")
                }

            }

            override fun onFailure(call: Call<RefreshResponse>, t: Throwable) {
                Log.d("SplashRefresh", "onFailure")
                splashView.onVerifyTokenFailure(t.message ?: "통신 오류")
            }
        })
    }
}