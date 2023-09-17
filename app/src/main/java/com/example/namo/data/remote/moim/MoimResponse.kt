package com.example.namo.data.remote.moim

import androidx.room.ColumnInfo
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
    @SerializedName("moimUsers") var moimUsers : List<MoimListUser>
) : Serializable

data class MoimListUser (
    @SerializedName("userId") var userId : Long,
    @SerializedName("userName") var userName : String,
    @SerializedName("userColor") var color : Int
) : Serializable

data class MoimListUserList(
    var memberList : List<MoimListUser>
) : Serializable

data class MoimScheduleUser (
    @SerializedName("userId") var userId : Long,
    @SerializedName("userName") var userName : String,
    @SerializedName("color") var color : Int
) : Serializable

data class GetMoimScheduleResponse (
    @SerializedName("result") val result : List<MoimSchedule>
) : BaseResponse()

data class MoimSchedule(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startDate : Long = 0L,
    @SerializedName("endDate") var endDate : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("users") var users : List<MoimScheduleUser> = listOf(),
    @SerializedName("moimId") var moimId : Long = 0L,
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
    @SerializedName("x") var placeX: Double = 0.0,
    @SerializedName("y") var placeY: Double = 0.0,
    @SerializedName("locationName") val placeName: String = "없음",
    @SerializedName("hasDiaryPlace") val hasPlace: Boolean = false,
    @SerializedName("curMoimSchedule") var curMoimSchedule : Boolean = false
) : Serializable

data class ParticipateMoimResponse(
    @SerializedName("result") val result : Long = 0L
) : BaseResponse()

data class UpdateMoimNameBody(
    val moimId: Long,
    val moimName: String
)

data class AddMoimScheduleResponse(
    @SerializedName("result") val result : Long
) : BaseResponse()