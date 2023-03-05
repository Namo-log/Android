package com.example.namo.data.entity.diary.retrofit

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class DiaryResponse {

    /** 기록 생성 **/
    data class DiaryAddResponse  (
        @SerializedName("code") val code : Int,
        @SerializedName("message") val message : String,
        @SerializedName("result") val result : GetScheduleIdx
    )

    data class GetScheduleIdx(
        @SerializedName("scheduleIdx") val scheduleIdx:Int
    )


    /** 기록 수정 **/
    data class DiaryEditResponse(
        @SerializedName("code") val code: Int,
        @SerializedName("message") val message: String,
        @SerializedName("result") val result : String
    )


    /** 기록 삭제 **/
    data class DiaryDeleteResponse(
        @SerializedName("code") val code: Int,
        @SerializedName("message") val message: String,
        @SerializedName("result") val result : String
    )


    /** 기록 월 별 조회 **/
    data class DiaryGetMonthResponse(
        @SerializedName("code") val code : Int,
        @SerializedName("message") val message : String,
        @SerializedName("result") val result :List<MonthDiaryDto>
    )

    data class MonthDiaryDto(
        @SerializedName("name") val title: String,
        @SerializedName("scheduleId") val scheduleIdx: Int,
        @SerializedName("startDate") var startDate: Long,
        @SerializedName("texts") val content: String,
        @SerializedName("urls") val imgUrl: List<String>
        ) : Serializable
}

