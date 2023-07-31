package com.example.namo.data.remote.event

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

data class PostEventResponse (
    @SerializedName("result") val result : PostEventResult
) : BaseResponse()

data class PostEventResult (
    @SerializedName("scheduleIdx")  val eventIdx : Long
)

data class EditEventResponse (
    @SerializedName("result") val result : EditEventResult
) : BaseResponse()

data class EditEventResult (
    @SerializedName("scheduleIdx")  val eventIdx : Long
)

data class DeleteEventResponse (
    @SerializedName("result") val result : String
) : BaseResponse()