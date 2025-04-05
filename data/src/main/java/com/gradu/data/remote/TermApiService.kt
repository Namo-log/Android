package com.gradu.data.remote

import com.mongmong.namo.data.dto.TermBody
import com.mongmong.namo.domain.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TermApiService {
    // 약관 동의
    @POST("terms")
    suspend fun postTermsCheck(
        @Body termBody: TermBody
    ): BaseResponse
}