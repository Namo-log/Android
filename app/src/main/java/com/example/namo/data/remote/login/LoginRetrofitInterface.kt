package com.example.namo.data.remote.login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginRetrofitInterface {
    // SDK 카카오 로그인
    @POST("auth/kakao/signup")
    fun postKakaoSDK(
        @Body body: TokenBody
    ) : Call<KakaoSDKResponse>
}