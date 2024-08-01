package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.auth.RemoteAuthDataSource
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {
    override suspend fun postKakaoLogin(body: LoginBody): LoginResponse {
        return remoteAuthDataSource.postKakaoLogin(body)
    }

    override suspend fun postNaverLogin(body: LoginBody): LoginResponse {
        return remoteAuthDataSource.postNaverLogin(body)
    }

    override suspend fun postTokenRefresh(
        accessToken: String,
        refreshToken: String
    ): RefreshResponse {
        return remoteAuthDataSource.postTokenRefresh(TokenBody(accessToken, refreshToken))
    }

    override suspend fun postLogout(accessToken: String): Boolean {
        return remoteAuthDataSource.postLogout(LogoutBody(accessToken))
    }

    override suspend fun postKakaoQuit(): Boolean {
        return remoteAuthDataSource.postKakaoQuit()
    }

    override suspend fun postNaverQuit(): Boolean {
        return remoteAuthDataSource.postNaverQuit()
    }
}