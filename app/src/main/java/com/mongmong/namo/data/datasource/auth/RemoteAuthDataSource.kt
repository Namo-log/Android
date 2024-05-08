package com.mongmong.namo.data.datasource.auth

import android.util.Log
import com.mongmong.namo.data.remote.LoginApiService
import com.mongmong.namo.domain.model.AccessTokenBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.model.TokenBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteAuthDataSource @Inject constructor(
    private val loginApiService: LoginApiService
) {
    suspend fun postKakaoLogin(
        tokenBody: AccessTokenBody
    ): LoginResponse {
        var loginResponse = LoginResponse(
            result = LoginResult(
                accessToken = "",
                refreshToken = "",
                newUser = false
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                loginApiService.postKakaoSDK(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postKakaoLogin Success $it")
                loginResponse = it
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postKakaoLogin Fail $it")
            }
        }
        return loginResponse
    }

    suspend fun postNaverLogin(
        tokenBody: AccessTokenBody
    ): LoginResponse {
        var loginResponse = LoginResponse(
            result = LoginResult(
                accessToken = "",
                refreshToken = "",
                newUser = false
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                loginApiService.postNaverSDK(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postNaverLogin Success $it")
                loginResponse = it
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postNaverLogin Fail $it")
            }
        }
        return loginResponse
    }
}