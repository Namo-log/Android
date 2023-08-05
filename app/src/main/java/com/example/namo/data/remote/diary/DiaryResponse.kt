package com.example.namo.data.remote.diary

import com.example.namo.config.BaseResponse
import com.google.gson.annotations.SerializedName

class DiaryResponse {

    /** 기록 생성 **/
    data class DiaryAddResponse(
        val result: GetScheduleIdx
    ) : BaseResponse()

    data class GetScheduleIdx(
        val scheduleIdx: Long
    )


    /** 기록 수정 **/
    data class DiaryEditResponse(
        val result: String
    ) : BaseResponse()


    /** 기록 삭제 **/
    data class DiaryDeleteResponse(
        val result: String
    ) : BaseResponse()


    /** 기록 월 별 조회 **/
    data class DiaryGetMonthResponse(
        val result: Result
    ) : BaseResponse()

    data class Result(
        val content: List<MonthDiary>,
        val currentPage: Int,
        val size: Int,
        val first: Boolean,
        val last: Boolean
    )

    data class MonthDiary(
        @SerializedName("scheduleId") val scheduleIdx: Long,
        @SerializedName("name") val title: String,
        @SerializedName("startDate") var startDate: String,
        @SerializedName("contents") val content: String?,
        @SerializedName("urls") val imgUrl: List<String>,
        @SerializedName("categoryId") val categoryId: Long,
        @SerializedName("color") val categoryColor: Long,
        @SerializedName("placeName") val placeName: String
    )

    /** 기록 일 별 조회 **/
    data class DiaryGetDayResponse(
        val result: DayDiaryDto
    ) : BaseResponse()

    data class DayDiaryDto(
        @SerializedName("texts") val content: String?,
        @SerializedName("urls") val imgUrl: List<String>?
    )
}

