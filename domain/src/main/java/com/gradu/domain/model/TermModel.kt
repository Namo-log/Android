package com.gradu.domain.model

import com.google.gson.annotations.SerializedName

data class TermBody(
    @SerializedName("isCheckTermOfUse") val isCheckTerm: Boolean,
    @SerializedName("isCheckPersonalInformationCollection") val isCheckPolicy: Boolean
)
