package com.mongmong.namo.presentation.config

import android.util.Log
import com.mongmong.namo.data.remote.ReissuanceApiService
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class XAccessTokenInterceptor @Inject constructor(
    private val apiService: ReissuanceApiService
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val accessToken: String? = runBlocking { dsManager.getAccessToken().first() }
        val refreshToken: String? = runBlocking { dsManager.getRefreshToken().first() }

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
                    Log.d("RefreshToken", "$refreshToken")
                }

                // 재발급 api 호출
                // 받은 토큰 다시 넣어서 기존 api 재호출
                val newTokenResponse = runBlocking { apiService.refreshToken(TokenBody(accessToken.toString(), refreshToken.toString())) }

                Log.d("Token", newTokenResponse.toString())

                if (newTokenResponse != null) {
                    runBlocking {
                        dsManager.saveAccessToken(newTokenResponse.result.accessToken)
                        dsManager.saveRefreshToken(newTokenResponse.result.refreshToken)
                    }
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