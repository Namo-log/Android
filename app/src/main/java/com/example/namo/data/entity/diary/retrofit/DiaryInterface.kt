package com.example.namo.data.entity.diary.retrofit

import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryInterface {

    /** 기록 생성 **/
    @Multipart
    @POST("/schedules/diary")
    fun addDiary(
        @Part imgs: List<MultipartBody.Part?>?,
        @Part("content") content: RequestBody?,
        @Part("scheduleId") scheduleIdx: RequestBody
    ) :Call<DiaryResponse.DiaryAddResponse>


    /** 기록 편집 **/
    @Multipart
    @PATCH("/schedules/diary")
    fun editDiary(
        @Part imgs: List<MultipartBody.Part?>?,
        @Part("content") content: RequestBody?,
        @Part("scheduleId") scheduleIdx: RequestBody
    ) :Call<DiaryResponse.DiaryEditResponse>


    /** 기록 삭제 **/
    @DELETE("/schedules/diary/{schedule}")
    fun deleteDiary(
        @Path ("schedule") schedule:Int
    ): Call<DiaryResponse.DiaryDeleteResponse>


    /** 기록 월 별 조회 **/
    @GET("/schedules/diary/{month}")
    fun getMonthDiary(
        @Path ("month") month:String
    ): Call<DiaryResponse.DiaryGetMonthResponse>

}

