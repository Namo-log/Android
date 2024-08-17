package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
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