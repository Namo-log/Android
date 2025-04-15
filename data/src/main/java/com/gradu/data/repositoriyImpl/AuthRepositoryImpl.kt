package com.gradu.data.repositoriyImpl

import com.gradu.data.datasource.auth.RemoteAuthDataSource
import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.LoginBody
import com.gradu.domain.model.LoginResponse
import com.gradu.domain.model.RefreshResponse
import com.gradu.domain.model.RegisterInfo
import com.gradu.domain.repositories.AuthRepository
import com.gradu.data.utils.mappers.AuthMapper.toDTO
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