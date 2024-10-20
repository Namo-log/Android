package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.auth.RemoteAuthDataSource
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {

    override suspend fun postLogin(loginPlatform: String, body: LoginBody): LoginResponse {
        return remoteAuthDataSource.postLogin(loginPlatform, body)
    }

    override suspend fun postTokenRefresh(): RefreshResponse {
        return remoteAuthDataSource.postTokenRefresh()
    }

    override suspend fun postLogout(): Boolean {
        return remoteAuthDataSource.postLogout()
    }

    override suspend fun postQuit(loginPlatform: String): Boolean {
        return remoteAuthDataSource.postQuit(loginPlatform)
    }
}