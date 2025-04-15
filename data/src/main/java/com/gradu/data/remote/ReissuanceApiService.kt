package com.gradu.data.remote

import com.gradu.domain.model.RefreshResponse
import retrofit2.http.POST

/** 추후 헤더 없이 AnonymousApiService로 이전 예정 */
interface ReissuanceApiService {
    // 토큰 재발급
    @POST("auths/reissuance")
    suspend fun refreshToken(
    ): RefreshResponse
}