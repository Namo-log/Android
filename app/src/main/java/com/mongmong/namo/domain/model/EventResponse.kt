package com.mongmong.namo.domain.model

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName

data class PostScheduleResponse (
    val result : PostScheduleResult
) : BaseResponse()

data class PostScheduleResult (
    @SerializedName("scheduleId") val scheduleId : Long
)

data class EditScheduleResponse (
    @SerializedName("result") val result : EditScheduleResult
) : BaseResponse()

data class EditScheduleResult (
    @SerializedName("scheduleId")  val scheduleId : Long
)

data class DeleteScheduleResponse (
    @SerializedName("result") val result : String
) : BaseResponse()

data class GetMonthScheduleResponse (
    @SerializedName("result") val result : List<GetMonthScheduleResult>
) : BaseResponse()

data class GetMonthScheduleResult (
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