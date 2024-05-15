package com.mongmong.namo.presentation.config

import android.util.Log
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.X_REFRESH_TOKEN
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.sSharedPreferences
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.domain.repositories.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class XAccessTokenInterceptor @Inject constructor(
    private val apiService: AuthApiService
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken: String? = sSharedPreferences.getString(X_ACCESS_TOKEN, null)
        val refreshToken: String = sSharedPreferences.getString(X_REFRESH_TOKEN, null).toString()

        val newRequest = request.newBuilder()

        if (accessToken != null) {
            newRequest.addHeader("Authorization", "Bearer $accessToken")
        }

        val response = chain.proceed(newRequest.build())

        when (response.code) {
            400 -> {
                // Show Bad Request Error Message
            }
            401 -> {
                // Show Forbidden Message
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
                val newTokenResponse = runBlocking { apiService.refreshToken(TokenBody(accessToken.toString(), refreshToken)) }

                Log.d("Token", newTokenResponse.toString())

                if (newTokenResponse != null) {
                    sSharedPreferences.edit()
                        .putString(X_REFRESH_TOKEN, newTokenResponse.result.refreshToken)
                        .putString(X_ACCESS_TOKEN, newTokenResponse.result.accessToken)
                        .apply()
                    Log.d("onTokenResponse", "토큰 재발급 성공!")

                    val newJwtToken = newTokenResponse.result.accessToken

                    // 새로운 토큰으로 하던 작업 서버 재요청
                    val finalRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $newJwtToken")
                        .build()

                    response.close()
                    return chain.proceed(finalRequest)
                }

                if (newTokenResponse.code == 403) { // 리프레시 토큰 만료
                    Log.d("Token", "403 리프레시 토큰 만료")
                    //TODO: 로그인 다시하기

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