package com.mongmong.namo.presentation.config

import com.mongmong.namo.presentation.config.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.X_REFRESH_TOKEN
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.sSharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class ReissuanceTokenInterceptor @Inject constructor(
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

        return chain.proceed(newRequest.build())
    }
}