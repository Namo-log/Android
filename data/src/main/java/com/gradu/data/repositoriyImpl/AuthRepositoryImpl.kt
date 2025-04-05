package com.gradu.data.repositoriyImpl

import com.mongmong.namo.data.datasource.auth.RemoteAuthDataSource
import com.mongmong.namo.data.utils.mappers.AuthMapper.toDTO
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.RegisterInfo
import com.mongmong.namo.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {

    override suspend fun postLogin(loginPlatform: String, body: LoginBody): LoginResponse {
        return remoteAuthDataSource.postLogin(loginPlatform, body)
    }

    override suspend fun postSignupComplete(registerInfo: RegisterInfo): BaseResponse {
        return remoteAuthDataSource.postSignupComplete(registerInfo.toDTO())
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