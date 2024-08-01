package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import retrofit2.http.Body
import retrofit2.http.POST

/** 추후 헤더 없이 AnonymousApiService로 이전 예정 */
interface ReissuanceApiService {
    // 토큰 재발급
    @POST("auths/reissuance")
    suspend fun refreshToken(
        @Body body: TokenBody
    ): RefreshResponse
}