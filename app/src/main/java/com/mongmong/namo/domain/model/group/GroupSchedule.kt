package com.mongmong.namo.domain.model.group

import com.google.gson.annotations.SerializedName
import com.mongmong.namo.presentation.config.BaseResponse
import java.io.Serializable
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

data class AddMoimScheduleResponse(
    @SerializedName("result") val result : Long
) : BaseResponse()

open class BaseMoimScheduleRequestBody(
    @SerializedName("name") var name : String = "",
    @SerializedName("startDate") var startLong : Long = 0L,
    @SerializedName("endDate") var endLong : Long = 0L,
    @SerializedName("interval") var interval : Int = 0,
    @SerializedName("x") var x : Double = 0.0,
    @SerializedName("y") var y : Double = 0.0,
    @SerializedName("locationName") var locationName : String = "",
    @SerializedName("users") var users : List<Long> = listOf()
)

/** 모임 일정 생성 */
data class AddMoimScheduleRequestBody(
    @SerializedName("groupId") var groupId : Long = 0L,
) : BaseMoimScheduleRequestBody()

/** 모임 일정 수정 */
data class EditMoimScheduleRequestBody(
    @SerializedName("moimScheduleId") var moimScheduleId : Long = 0L,
) : BaseMoimScheduleRequestBody()

// 모임 일정 참석자 선택
data class MoimSchduleMemberList(
    var memberList : List<GroupMember>
) : Serializable