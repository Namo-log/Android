package com.mongmong.namo.domain.model

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AddGroupResponse (
    @SerializedName("result") val result : AddGroupResult
) : BaseResponse()

data class AddGroupResult (
    @SerializedName("moimId") val moimId : Long
)

data class GetGroupsResponse (
    @SerializedName("result") val result : List<Group>
) : BaseResponse()

data class Group(
    @SerializedName("groupId") var groupId : Long,
    @SerializedName("groupName") var groupName : String,
    @SerializedName("groupImgUrl") var groupImgUrl : String,
    @SerializedName("groupCode") var groupCode : String,
    @SerializedName("moimUsers") var moimUsers : List<MoimUser>
) : Serializable

data class MoimUser (
    @SerializedName("userId") var userId : Long,
    @SerializedName("userName") var userName : String,
    @SerializedName("color") var color : Int
) : Serializable

data class MoimListUserList(
    var memberList : List<MoimUser>
) : Serializable

data class GetMoimScheduleResponse (
    @SerializedName("result") val result : List<MoimSchedule>
) : BaseResponse()

data class MoimSchedule(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startDate : Long = 0L,
    @SerializedName("endDate") var endDate : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("users") var users : List<MoimUser> = listOf(),
    @SerializedName("moimId") var moimId : Long = 0L,
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
    @SerializedName("x") var x : Double = 0.0,
    @SerializedName("y") var y : Double = 0.0,
    @SerializedName("locationName") var locationName : String = "",
    @SerializedName("hasDiaryPlace") var hasDiaryPlace : Boolean = false,
    @SerializedName("curMoimSchedule") var curMoimSchedule : Boolean = false
) : Serializable

data class JoinGroupResponse(
    @SerializedName("result") val result : Long = 0L
) : BaseResponse()

data class UpdateMoimNameBody(
    val moimId: Long,
    val moimName: String
)

data class AddMoimScheduleResponse(
    @SerializedName("result") val result : Long
) : BaseResponse()

data class PatchMoimScheduleCategoryBody(
    @SerializedName("moimScheduleId") val moimScheduleId: Long,
    @SerializedName("categoryId") val categoryId : Long
)
data class MoimScheduleAlarmBody(
    @SerializedName("moimScheduleId") val moimScheduleId: Long,
    @SerializedName("alarmDates") val alarmDates : List<Int>
)