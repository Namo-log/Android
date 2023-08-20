package com.example.namo.data.remote.diary

import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryInterface {

    @Multipart
    @POST("/schedules/diary")
    fun addDiary(
        @Part("scheduleId") scheduleIdx: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse.DiaryAddResponse>


    @Multipart
    @PATCH("/schedules/diary")
    fun editDiary(
        @Part("scheduleId") scheduleIdx: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse.DiaryEditResponse>


    @DELETE("/schedules/diary/{scheduleId}")
    fun deleteDiary(
        @Path("scheduleId") scheduleId: Long
    ): Call<DiaryResponse.DiaryDeleteResponse>


    @GET("/schedules/diary/{month}")
    fun getMonthDiary(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<DiaryResponse.DiaryGetMonthResponse>


    @Multipart
    @POST("/moims/schedule/diary")
    fun addGroupDiary(
        @Part("moimScheduleId") scheduleIdx: RequestBody,
        @Part("name") place: RequestBody?, // String
        @Part("money") pay: RequestBody?,  // Int
        @Part("participants") member: List<RequestBody>?,  // List<Int>
        @Part imgs: List<MultipartBody.Part>?  // List<String>
    ): Call<DiaryResponse.AddGroupDiaryResponse>


    @GET("moims/schedule/{moimScheduleId}")
    fun getGroupDiary(
        @Path("moimScheduleId") scheduleIdx: Long
    ): Call<DiaryResponse.GetGroupDiaryResponse>
}

