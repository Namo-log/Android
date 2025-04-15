package com.gradu.data.remote

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.TermBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TermApiService {
    // 약관 동의
    @POST("terms")
    suspend fun postTermsCheck(
        @Body termBody: TermBody
    ): BaseResponse
}