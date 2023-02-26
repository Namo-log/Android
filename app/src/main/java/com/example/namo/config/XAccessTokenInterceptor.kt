package com.example.namo.config

import android.util.Log
import com.example.namo.config.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.example.namo.config.ApplicationClass.Companion.X_REFRESH_TOKEN
import com.example.namo.config.ApplicationClass.Companion.sSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class XAccessTokenInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val jwtToken: String? = sSharedPreferences.getString(X_ACCESS_TOKEN, null)
        val refreshToken: String = sSharedPreferences.getString(X_REFRESH_TOKEN, null).toString()


        val newRequest = request.newBuilder()

        if (jwtToken != null) {
            newRequest.addHeader("Authorization", "Bearer ${jwtToken}")
        }

        val response = chain.proceed(newRequest.build())

        when (response.code) {
            400 -> {
                // Show Bad Request Error Message
            }
            401 -> {
                Log.d("Token", "401 액세스 토큰 만료")
                // 이전 토큰
                if (jwtToken != null) {
                    Log.d("AccessToken", jwtToken)
                    Log.d("RefreshToken", refreshToken)
                }

                // 재발급 api 호출
                // 받은 토큰 다시 넣어서 기존 api 재호출
            }
            403 -> {
                // Show Forbidden Message
            }
            404 -> {
                // Show NotFound Message
            }
            // ... and so on
        }

        return response
    }
}