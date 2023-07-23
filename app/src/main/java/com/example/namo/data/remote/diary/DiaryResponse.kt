package com.example.namo.data.remote.diary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DiaryResponse {

    /** 기록 생성 **/
    data class DiaryAddResponse(
        val result: GetScheduleIdx,
        val localId :Int
    )

    data class GetScheduleIdx(
        val scheduleIdx: Int
    )


    /** 기록 수정 **/
    data class DiaryEditResponse(
        val result: String
    )


    /** 기록 삭제 **/
    data class DiaryDeleteResponse(
        val result: String
    )


    /** 기록 월 별 조회 **/
    data class DiaryGetMonthResponse(
        val result: List<MonthDiaryDto>
    )

    data class MonthDiaryDto(
        @SerializedName("name") val title: String,
        @SerializedName("scheduleId") val scheduleIdx: Int,
        @SerializedName("startDate") var startDate: Long,
        @SerializedName("texts") val content: String,
        @SerializedName("urls") val imgUrl: List<String>,
        @SerializedName("categoryId") val categoryId: Int,
        @SerializedName("eventId") val eventId:Int,
        @SerializedName("placeName") val placeName:String

    ) : Serializable


    /** 기록 일 별 조회 **/
    data class DiaryGetDayResponse(
        val result: DayDiaryDto
    )

    data class DayDiaryDto(
        @SerializedName("texts") val content: String,
        @SerializedName("urls") val imgUrl: List<String>
    ) : Serializable
}

