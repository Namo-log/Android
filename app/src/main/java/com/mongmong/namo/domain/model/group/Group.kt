package com.mongmong.namo.domain.model.group

import com.mongmong.namo.presentation.config.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/** 그룹 리스트 조회 */
data class GetGroupsResponse (
    @SerializedName("result") val result : List<Group>
) : BaseResponse()

data class Group(
    @SerializedName("groupId") var groupId : Long = 0L,
    @SerializedName("groupName") var groupName : String = "",
    @SerializedName("groupImgUrl") var groupImgUrl : String = "",
    @SerializedName("groupCode") var groupCode : String = "",
    @SerializedName("moimUsers") var groupMembers : List<GroupMember> = emptyList()
) : Serializable

data class GroupMember (
    @SerializedName("userId") var userId : Long,
    @SerializedName("userName") var userName : String,
    @SerializedName("color") var color : Int
) : Serializable

/** 그룹 생성 */
data class AddGroupResponse (
    @SerializedName("result") val result : AddGroupResult
) : BaseResponse()

data class AddGroupResult (
    @SerializedName("moimId") val groupId : Long
)

/** 그룹 참여 **/
data class JoinGroupResponse(
    @SerializedName("result") val result : Long = 0L
) : BaseResponse()

/** 그룹명 변경 */
data class UpdateGroupNameRequestBody(
    @SerializedName("moimId") val groupId: Long,
    @SerializedName("moimName") val groupName: String
)

/** 그룹의 일정 조회 */
data class GetMoimScheduleResponse (
    @SerializedName("result") val result : List<MoimScheduleBody>
) : BaseResponse()

data class MoimScheduleBody(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startDate : Long = 0L,
    @SerializedName("endDate") var endDate : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("users") var users : List<GroupMember> = listOf(),
    @SerializedName("moimId") var moimId : Long = 0L,
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
    @SerializedName("x") var x : Double = 0.0,
    @SerializedName("y") var y : Double = 0.0,
    @SerializedName("locationName") var locationName : String = "",
    @SerializedName("hasDiaryPlace") var hasDiaryPlace : Boolean = false,
    @SerializedName("curMoimSchedule") var curMoimSchedule : Boolean = false
) : Serializable

