package com.mongmong.namo.data.remote.diary

import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetGroupDiaryResponse
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryApiService {

    @Multipart
    @POST("/schedules/diary")
    fun addDiary(
        @Part("scheduleId") scheduleIdx: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryAddResponse


    @Multipart
    @PATCH("/schedules/diary")
    fun editDiary(
        @Part("scheduleId") scheduleIdx: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse>


    @DELETE("/schedules/diary/{scheduleId}")
    fun deleteDiary(
        @Path("scheduleId") scheduleId: Long
    ): Call<DiaryResponse>


    @GET("/schedules/diary/all")
    fun getAllDiary(): Call<DiaryGetAllResponse>

    @Multipart
    @POST("/moims/schedule/memo/{moimId}")
    fun addGroupDiary(
        @Path("moimId") scheduleIdx: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse>


    @GET("/moims/schedule/memo/{moimId}")
    fun getGroupDiary(
        @Path("moimId") scheduleIdx: Long
    ): Call<GetGroupDiaryResponse>


    @Multipart
    @PATCH("/moims/schedule/memo/{moimMemoLocationId}")
    fun patchGroupDiaryPlace(
        @Path("moimMemoLocationId") moimScheduldIdx: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): Call<DiaryResponse>

    @DELETE("/moims/schedule/memo/{moimMemoLocationId}")
    fun deleteGroupDiaryPlace(
        @Path("moimMemoLocationId") moimScheduldIdx: Long
    ): Call<DiaryResponse>


    @GET("/moims/schedule/memo/month/{month}")
    suspend fun getGroupMonthDiary(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): DiaryGetMonthResponse

    @GET("/moims/schedule/memo/month/{month}")
    fun getGroupMonthDiary2(
        @Path("month") month: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<DiaryGetMonthResponse>


    @PATCH("/moims/schedule/memo/text/{scheduleId}")
    fun addGroupAfterDiary(
        @Path("scheduleId") scheduleId: Long,
        @Body text: String?
    ): Call<DiaryResponse>
}
