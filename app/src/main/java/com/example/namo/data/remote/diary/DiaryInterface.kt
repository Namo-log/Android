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
    ): Call<DiaryResponse.DiaryResponse>


    @DELETE("/schedules/diary/{scheduleId}")
    fun deleteDiary(
        @Path("scheduleId") scheduleId: Long
    ): Call<DiaryResponse.DiaryResponse>


    @GET("/schedules/diary/all")
    fun getAllDiary(): Call<DiaryResponse.DiaryGetAllResponse>

    @Multipart
    @POST("/moims/schedule/memo/{moimId}")
    fun addGroupDiary(
        @Path("moimId") scheduleIdx: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse.DiaryResponse>


    @GET("/moims/schedule/memo/{moimId}")
    fun getGroupDiary(
        @Path("moimId") scheduleIdx: Long
    ): Call<DiaryResponse.GetGroupDiaryResponse>


    @Multipart
    @PATCH("/moims/schedule/memo/{moimMemoLocationId}")
    fun patchGroupDiaryPlace(
        @Path("moimMemoLocationId") moimScheduldIdx: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse.DiaryResponse>

    @DELETE("/moims/schedule/memo/{moimMemoLocationId}")
    fun deleteGroupDiaryPlace(
        @Path("moimMemoLocationId") moimScheduldIdx: Long
    ): Call<DiaryResponse.DiaryResponse>


    @GET("/moims/schedule/memo/month/{month}")
    suspend fun getGroupMonthDiary(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): DiaryResponse.DiaryGetMonthResponse

    @GET("/moims/schedule/memo/month/{month}")
    fun getGroupMonthDiary2(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<DiaryResponse.DiaryGetMonthResponse>


    @PATCH("/moims/schedule/memo/text/{scheduleId}")
    fun addGroupAfterDiary(
        @Path("scheduleId") scheduleId: Long,
        @Body text: String?
    ): Call<DiaryResponse.DiaryResponse>
}

