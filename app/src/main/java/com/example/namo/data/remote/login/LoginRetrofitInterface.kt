package com.example.namo.data.remote.login

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginRetrofitInterface {
    // SDK 카카오 로그인
    @POST("v1/auth/kakao")
    fun postKakaoSDK(
        @Body body: SdkTokenBody
    ) : Call<KakaoSDKResponse>
}