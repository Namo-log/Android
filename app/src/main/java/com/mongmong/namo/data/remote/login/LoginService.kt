package com.mongmong.namo.data.remote.login

import android.util.Log
import com.mongmong.namo.config.ApplicationClass
import com.mongmong.namo.ui.login.LoginFragment
import com.navercorp.nid.oauth.OAuthLoginCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginService(val view: LoginFragment) {

    val loginRetrofitInterface = ApplicationClass.bRetrofit.create(LoginRetrofitInterface::class.java)

    fun tryPostKakaoSDK(body: TokenBody) {
        loginRetrofitInterface.postKakaoSDK(body).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                view.onPostKakaoSDKSuccess(response.body() as LoginResponse)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("KakaoLogin", "onFailure")
                view.onPostKakaoSDKFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun tryPostNaverSDK(body: TokenBody) {
        loginRetrofitInterface.postNaverSDK(body).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                view.onPostNaverSDKSuccess(response.body() as LoginResponse)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("NaverLogin", "onFailure")
                view.onPostNaverSDKFailure(t.message ?: "통신 오류")
            }
        })
    }
}