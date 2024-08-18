package com.mongmong.namo.presentation.config

import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class ReissuanceTokenInterceptor @Inject constructor(
) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val accessToken: String? = runBlocking { dsManager.getAccessToken().first() }
        val refreshToken: String? = runBlocking { dsManager.getRefreshToken().first() }

        val newRequest = request.newBuilder()

        if (accessToken != null) {
            newRequest.addHeader("Authorization", "Bearer $accessToken")
            newRequest.addHeader("refreshToken", refreshToken.toString())
        }

        return chain.proceed(newRequest.build())
    }
}