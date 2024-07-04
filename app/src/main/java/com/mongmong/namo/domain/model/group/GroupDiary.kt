package com.mongmong.namo.domain.model.group

import com.google.gson.annotations.SerializedName
import com.mongmong.namo.presentation.config.BaseResponse

/** 모임 기록 개별 조회 **/
data class GetMoimDiaryResponse(
    val result: MoimDiaryResult
) : BaseResponse()

data class MoimDiaryResult(
    val name: String,
    val startDate: Long,
    val locationName: String,
    val users: List<MoimScheduleMember>,
    @SerializedName("moimActivityDtos") val moimActivities: List<MoimActivity>
)

data class MoimScheduleMember(
    val userId: Long,
    val userName: String
) : java.io.Serializable

data class MoimActivity(
    @SerializedName("moimActivityId") val moimActivityId: Long = 0L,
    var name: String = "",
    @SerializedName("money") var pay: Long = 0L,
    @SerializedName("participants") var members: List<Long>,
    @SerializedName("urls") var imgs: List<String>?
) : java.io.Serializable {
    companion object {
        fun getDefault() = MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf())
    }
}