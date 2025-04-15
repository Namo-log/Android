package com.gradu.data.remote

import com.gradu.domain.model.LoginBody
import com.gradu.domain.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AnonymousApiService {
    @POST("auths/signup/{socialType}")
    suspend fun postLogin(
        @Path("socialType") socialPlatform: String,
        @Body body: LoginBody
    ): LoginResponse
}