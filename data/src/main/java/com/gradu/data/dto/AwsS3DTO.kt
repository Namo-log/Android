package com.gradu.data.dto

import com.mongmong.namo.domain.model.BaseResponse

data class GetPreSignedUrlResponse(
    val result: String
): BaseResponse()