package com.mongmong.namo.domain.model

import com.google.gson.annotations.SerializedName
import com.mongmong.namo.presentation.config.BaseResponse

data class TermResponse(
    val result: String // 기본 string
) : BaseResponse()

data class TermBody(
    @SerializedName("isCheckTermOfUse") val isCheckTerm: Boolean,
    @SerializedName("isCheckPersonalInformationCollection") val isCheckPolicy: Boolean
)