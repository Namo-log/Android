package com.mongmong.namo.data.datasource.auth

import android.util.Log
import com.mongmong.namo.data.remote.AnonymousApiService
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.data.remote.ReissuanceApiService
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.RefreshResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteAuthDataSource @Inject constructor(
    private val authApiService: AuthApiService,
    private val anonymousApiService: AnonymousApiService,
    private val reissuanceApiService: ReissuanceApiService
) {
    suspend fun postLogin(
        loginPlatform: String,
        tokenBody: LoginBody
    ): LoginResponse {
        var loginResponse = LoginResponse(
            result = LoginResult()
        )
        withContext(Dispatchers.IO) {
            runCatching {
                anonymousApiService.postLogin(loginPlatform, tokenBody)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postLogin Success $it")
                loginResponse = it
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postLogin Fail $it")
            }
        }
        return loginResponse
    }

    suspend fun postTokenRefresh(): RefreshResponse {
        var refreshResponse = RefreshResponse(
            result = RefreshResult(
                accessToken = "",
                refreshToken = ""
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                reissuanceApiService.refreshToken()
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postTokenRefresh Success $it")
                refreshResponse = it
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postTokenRefresh Fail $it")
            }
        }
        return refreshResponse
    }

    suspend fun postLogout(): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                authApiService.postLogout()
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postLogout Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postLogout Fail $it")
            }
        }
        return isSuccess
    }

    suspend fun postQuit(
        loginPlatform: String
    ): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                authApiService.postQuit(loginPlatform)
            }.onSuccess {
                Log.d("RemoteAuthDataSource", "postQuit Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteAuthDataSource", "postQuit Fail $it")
            }
        }
        return isSuccess
    }
}