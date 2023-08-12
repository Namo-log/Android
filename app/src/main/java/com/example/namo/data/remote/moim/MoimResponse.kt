package com.example.namo.data.remote.moim

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddMoimResponse (
    @SerializedName("result") val result : AddMoimResult
) : BaseResponse()

data class AddMoimResult (
    @SerializedName("moimId") val moimId : Long
)

data class GetMoimListResponse (
    @SerializedName("result") val result : List<Moim>
) : BaseResponse()

data class Moim(
    @SerializedName("groupId") var groupId : Long,
    @SerializedName("groupName") var groupName : String,
    @SerializedName("groupImgUrl") var groupImgUrl : String,
    @SerializedName("groupCode") var groupCode : String,
    @SerializedName("moimUsers") var moimUsers : List<MoimUser>
) : Serializable

data class MoimUser (
    @SerializedName("userId") var userId : Long,
    @SerializedName("userName") var userName : String,
    @SerializedName("userColor") var paletteId : Int
) : Serializable

data class GetMoimScheduleResponse (
    @SerializedName("result") val result : List<MoimSchedule>
) : BaseResponse()

data class MoimSchedule(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startDate : Long = 0L,
    @SerializedName("endDate") var endDate : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("users") var users : List<MoimUser>,
    @SerializedName("moimId") var moimId : Long?,
    @SerializedName("moimScheduleId") var moimScheduleId : Long?,
    @SerializedName("curMoimSchedule") var curMoimSchedule : Boolean
)

data class ParticipateMoimResponse(
    @SerializedName("result") val result : Long
) : BaseResponse()