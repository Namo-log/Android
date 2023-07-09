package com.example.namo.config

import android.util.Log
import com.example.namo.config.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.example.namo.config.ApplicationClass.Companion.X_REFRESH_TOKEN
import com.example.namo.config.ApplicationClass.Companion.sSharedPreferences
import com.example.namo.data.remote.login.RefreshService
import com.example.namo.data.remote.login.TokenBody
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class XAccessTokenInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken: String? = sSharedPreferences.getString(X_ACCESS_TOKEN, null)
        val refreshToken: String = sSharedPreferences.getString(X_REFRESH_TOKEN, null).toString()

        val newRequest = request.newBuilder()

        if (accessToken != null) {
            newRequest.addHeader("Authorization", accessToken)
        }

        val response = chain.proceed(newRequest.build())

        when (response.code) {
            400 -> {
                // Show Bad Request Error Message
            }
            401 -> {
                Log.d("Token", "401 액세스 토큰 만료")
                // 이전 토큰
                if (accessToken != null) {
                    Log.d("AccessToken", accessToken)
                    Log.d("RefreshToken", refreshToken)
                }

                // 재발급 api 호출
                // 받은 토큰 다시 넣어서 기존 api 재호출
                val newTokenResponse = RefreshService().tryTokenRefresh(TokenBody(accessToken.toString(), refreshToken))
                val responseBody = newTokenResponse.body()

                Log.d("Token", newTokenResponse.toString())

                if (responseBody != null) {
                    sSharedPreferences.edit()
                        .putString(X_REFRESH_TOKEN, responseBody.result.refreshToken)
                        .putString(X_ACCESS_TOKEN, responseBody.result.accessToken)
                        .apply()
                    Log.d("onTokenResponse", "토큰 재발급 성공!")
                    Log.d("onTokenResponse", "${newTokenResponse.code()}\n" + "${newTokenResponse.body()!!.result}")

                    //val newJwtToken: String? = sSharedPreferences.getString(X_ACCESS_TOKEN, null)
                    val newJwtToken = responseBody.result.accessToken

                    // 새로운 토큰으로 하던 작업 서버 재요청
                    val finalRequest = chain.request().newBuilder()
                        .addHeader("Authorization", newJwtToken)
                        .build()

                    response.close()
                    return chain.proceed(finalRequest)
                }

                if (newTokenResponse.body()?.code == 401) { // 리프레시 토큰 만료
                    Log.d("Token", "401 리프레시 토큰 만료")
                    // 로그인 다시하기

                }
            }
            403 -> {
                // Show Forbidden Message
                Log.d("Token", "403 액세스 토큰 만료")
                // 이전 토큰
                if (accessToken != null) {
                    Log.d("AccessToken", accessToken)
                    Log.d("RefreshToken", refreshToken)
                }

                // 재발급 api 호출
                // 받은 토큰 다시 넣어서 기존 api 재호출
                val newTokenResponse = RefreshService().tryTokenRefresh(TokenBody(accessToken.toString(), refreshToken))
                val responseBody = newTokenResponse.body()

                Log.d("Token", newTokenResponse.toString())

                if (responseBody != null) {
                    sSharedPreferences.edit()
                        .putString(X_REFRESH_TOKEN, responseBody.result.refreshToken)
                        .putString(X_ACCESS_TOKEN, responseBody.result.accessToken)
                        .apply()
                    Log.d("onTokenResponse", "토큰 재발급 성공!")
                    Log.d("onTokenResponse", "${newTokenResponse.code()}\n" + "${newTokenResponse.body()!!.result}")

                    //val newJwtToken: String? = sSharedPreferences.getString(X_ACCESS_TOKEN, null)
                    val newJwtToken = responseBody.result.accessToken

                    // 새로운 토큰으로 하던 작업 서버 재요청
                    val finalRequest = chain.request().newBuilder()
                        .addHeader("Authorization", newJwtToken)
                        .build()

                    response.close()
                    return chain.proceed(finalRequest)
                }

                if (newTokenResponse.body()?.code == 401) { // 리프레시 토큰 만료
                    Log.d("Token", "401 리프레시 토큰 만료")
                    // 로그인 다시하기

                }
            }
            404 -> {
                // Show NotFound Message
            }
            // ... and so on
        }

        return response
    }
}