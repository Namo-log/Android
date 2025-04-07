package com.gradu.data.dto

import com.google.gson.annotations.SerializedName
import com.gradu.domain.model.BaseResponse

data class TermResponse(
    val result: String // 기본 string
) : BaseResponse()

data class TermBody(
    @SerializedName("isCheckTermOfUse") val isCheckTerm: Boolean,
    @SerializedName("isCheckPersonalInformationCollection") val isCheckPolicy: Boolean
)