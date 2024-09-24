package com.mongmong.namo.data.dto

import com.mongmong.namo.presentation.config.BaseResponse

data class GetPreSignedUrlResponse(
    val result: String
): BaseResponse()