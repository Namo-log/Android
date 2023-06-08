package com.example.namo.data.remote.login

import android.util.Log
import com.example.namo.config.ApplicationClass
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(val view: LoginView) {

    val loginRetrofitInterface = ApplicationClass.bRetrofit.create(LoginRetrofitInterface::class.java)
    fun tryPostKakaoSDK(body: TokenBody) {
        loginRetrofitInterface.postKakaoSDK(body).enqueue(object : Callback<KakaoSDKResponse> {

            override fun onResponse(call: Call<KakaoSDKResponse>, response: Response<KakaoSDKResponse>) {
                view.onPostKakaoSDKSuccess(response.body() as KakaoSDKResponse)
            }

            override fun onFailure(call: Call<KakaoSDKResponse>, t: Throwable) {
                Log.d("KakaoLogin", "onFailure")
                view.onPostKakaoSDKFailure(t.message ?: "통신 오류")
            }
        })
    }
}