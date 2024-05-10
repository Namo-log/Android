package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.auth.RemoteAuthDataSource
import com.mongmong.namo.domain.model.AccessTokenBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {
    override suspend fun postKakaoLogin(accessToken: String): LoginResponse {
        return remoteAuthDataSource.postKakaoLogin(AccessTokenBody(accessToken))
    }

    override suspend fun postNaverLogin(accessToken: String): LoginResponse {
        return remoteAuthDataSource.postNaverLogin(AccessTokenBody(accessToken))
    }

    override suspend fun postLogout(accessToken: String): Boolean {
        return remoteAuthDataSource.postLogout(LogoutBody(accessToken))
    }

    override suspend fun postKakaoQuit(accessToken: String): Boolean {
        return remoteAuthDataSource.postKakaoQuit(AccessTokenBody(accessToken))
    }

    override suspend fun postNaverQuit(accessToken: String): Boolean {
        return remoteAuthDataSource.postNaverQuit(AccessTokenBody(accessToken))
    }
}