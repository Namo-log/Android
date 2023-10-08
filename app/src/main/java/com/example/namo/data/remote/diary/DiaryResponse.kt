package com.example.namo.data.remote.diary

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

class DiaryResponse {

    data class DiaryResponse(
        val result: String
    ) : BaseResponse() // 기본 string


    /** 기록 추가 **/
    data class DiaryAddResponse(
        val result: GetScheduleIdx
    ) : BaseResponse()

    data class GetScheduleIdx(
        val scheduleIdx: Long
    )

    /** 기록 월 별 조회 **/
    data class DiaryGetAllResponse(
        val result: List<Result>
    ) : BaseResponse()

    data class Result(
        val scheduleId: Long,
        val contents: String?,
        val urls: List<String>,
    )


    /** 모임 기록 개별 조회 **/
    data class GetGroupDiaryResponse(
        val result: GroupDiaryResult
    ) : BaseResponse()

    data class GroupDiaryResult(
        val name:String,
        val startDate: Long,
        val locationName: String,
        val users: List<GroupUser>,
        val locationDtos: List<LocationDto>
    )

    data class GroupUser(
        val userId: Long,
        val userName: String
    ) : java.io.Serializable

    data class LocationDto(
        val moimMemoLocationId: Long,
        @SerializedName("name") var place: String,
        @SerializedName("money") var pay: Long,
        @SerializedName("participants") var members: MutableList<Long>,
        @SerializedName("urls") var imgs: List<String>
    ) : java.io.Serializable


    /** 모임 기록 월 별 조회 **/
    data class DiaryGetMonthResponse(
        val result: GroupResult
    ) : BaseResponse()

    data class GroupResult(
        val content: List<MonthDiary>,
        val currentPage: Int,
        val size: Int,
        val first: Boolean,
        val last: Boolean
    )

    data class MonthDiary(
        @SerializedName("scheduleId") val scheduleIdx: Long,
        @SerializedName("name") val title: String,
        @SerializedName("startDate") var startDate: Long,
        @SerializedName("contents") val content: String?,
        @SerializedName("urls") val imgUrl: List<String>,
        @SerializedName("categoryId") val categoryId: Long,
        @SerializedName("color") val categoryColor: Long,
        @SerializedName("placeName") val placeName: String
    ):java.io.Serializable
}

