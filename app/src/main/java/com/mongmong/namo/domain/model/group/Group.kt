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
    @SerializedName("groupUsers") var groupMembers : List<GroupMember> = emptyList()
) : Serializable {
    fun getMemberNames(): String {
        return groupMembers.joinToString { it.userName }
    }
}

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
    @SerializedName("groupId") val groupId : Long
)

/** 그룹 참여 **/
data class JoinGroupResponse(
    val result : JoinGroupResult
) : BaseResponse()

data class JoinGroupResult(
    val groupId: Long,
    @SerializedName("code") val groupCode: String
)

/** 그룹명 변경 */
data class UpdateGroupNameRequestBody(
    @SerializedName("groupId") val groupId: Long,
    @SerializedName("groupName") val groupName: String
)

data class UpdateGroupNameResponse(
    val result : Long = 0L
) : BaseResponse()

/** 그룹의 일정 조회 */
data class GetMoimScheduleResponse (
    @SerializedName("result") val result : List<MoimScheduleBody>
) : BaseResponse()

data class MoimScheduleBody(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startLong : Long = 0L,
    @SerializedName("endDate") var endLong : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("x") var placeX : Double = 0.0,
    @SerializedName("y") var placeY : Double = 0.0,
    @SerializedName("locationName") var placeName : String = "",
    @SerializedName("users") var members : List<GroupMember> = listOf(),
    @SerializedName("groupId") var groupId : Long = 0L,
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
    @SerializedName("hasDiaryPlace") var hasDiaryPlace : Boolean = false,
    @SerializedName("curMoimSchedule") var curMoimSchedule : Boolean = false
) : Serializable {
    fun convertMoimScheduleToBaseRequest(): BaseMoimScheduleRequestBody {
        return BaseMoimScheduleRequestBody(
            this.name,
            this.startLong,
            this.endLong,
            this.interval,
            this.placeX,
            this.placeY,
            this.placeName,
            this.members.map { user -> user.userId } as ArrayList<Long>,
        )
    }
}


fun convertToGroupMembers(members: List<GroupMember>): List<MoimScheduleMember> {
    return members.map { groupMember ->
        MoimScheduleMember(
            userId = groupMember.userId,
            userName = groupMember.userName
        )
    }
}