package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.auth.RemoteAuthDataSource
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteAuthDataSource: RemoteAuthDataSource
) : AuthRepository {
    override suspend fun postKakaoLogin(accessToken: String, refreshToken: String): LoginResponse {
        return remoteAuthDataSource.postKakaoLogin(TokenBody(accessToken, refreshToken))
    }

    override suspend fun postNaverLogin(accessToken: String, refreshToken: String): LoginResponse {
        return remoteAuthDataSource.postNaverLogin(TokenBody(accessToken, refreshToken))
    }
}