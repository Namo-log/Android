package com.gradu.data.dto

import com.gradu.domain.model.BaseResponse


data class GetPreSignedUrlResponse(
    val result: String
): BaseResponse()