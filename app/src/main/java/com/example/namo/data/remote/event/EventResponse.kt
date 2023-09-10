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

data class GetMonthEventResponse (
    @SerializedName("result") val result : List<GetMonthEventResult>
) : BaseResponse()

data class GetMonthEventResult (
    @SerializedName("scheduleId") val scheduleId : Long,
    @SerializedName("name") val name : String,
    @SerializedName("startDate") val startDate : Long,
    @SerializedName("endDate") val endDate : Long,
    @SerializedName("alarmDate") val alarmDate : List<Int>,
    @SerializedName("interval") val interval : Int,
    @SerializedName("x") val x : Double,
    @SerializedName("y") val y : Double,
    @SerializedName("locationName") val locationName : String,
    @SerializedName("categoryId") val categoryId : Long,
    @SerializedName("hasDiary") val hasDiary : Boolean,
    @SerializedName("moimSchedule") val moimSchedule : Boolean,
)