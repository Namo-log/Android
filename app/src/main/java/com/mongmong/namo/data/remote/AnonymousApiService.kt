package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AnonymousApiService {
    // SDK 카카오 로그인
    @POST("auths/kakao/signup")
    suspend fun postKakaoSDK(
        @Body body: LoginBody
    ): LoginResponse

    // SDK 네이버 로그인
    @POST("auths/naver/signup")
    suspend fun postNaverSDK(
        @Body body: LoginBody
    ): LoginResponse

}