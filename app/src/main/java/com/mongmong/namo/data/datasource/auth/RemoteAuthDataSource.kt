package com.mongmong.namo.data.datasource.auth

import android.util.Log
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.domain.model.AccessTokenBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.model.LogoutBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteAuthDataSource @Inject constructor(
    private val authApiService: AuthApiService
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
                authApiService.postKakaoSDK(tokenBody)
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
                authApiService.postNaverSDK(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postNaverLogin Success $it")
                loginResponse = it
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postNaverLogin Fail $it")
            }
        }
        return loginResponse
    }

    suspend fun postLogout(
        tokenBody: LogoutBody
    ): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                authApiService.postLogout(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postLogout Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postLogout Fail $it")
            }
        }
        return isSuccess
    }

    suspend fun postKakaoQuit(
        tokenBody: AccessTokenBody
    ): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                authApiService.postKakaoQuit(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postKakaoQuit Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postKakaoQuit Fail $it")
            }
        }
        return isSuccess
    }

    suspend fun postNaverQuit(
        tokenBody: AccessTokenBody
    ): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                authApiService.postNaverQuit(tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postNaverQuit Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postNaverQuit Fail $it")
            }
        }
        return isSuccess
    }
}