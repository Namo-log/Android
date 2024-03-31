package com.mongmong.namo.data.remote.diary

import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetMoimDiaryResponse
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryApiService {

    // 개인 기록 추가
    @Multipart
    @POST("/schedules/diary")
    suspend fun addDiary(
        @Part("scheduleId") scheduleId: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryAddResponse

    // 개인 기록 수정
    @Multipart
    @PATCH("/schedules/diary")
    suspend fun editDiary(
        @Part("scheduleId") scheduleId: RequestBody,
        @Part("content") content: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryResponse

    // 개인 기록 삭제
    @DELETE("/schedules/diary/{scheduleId}")
    suspend fun deleteDiary(
        @Path("scheduleId") scheduleId: Long
    ): DiaryResponse


    @GET("/schedules/diary/all")
    fun getAllDiary(): Call<DiaryGetAllResponse>

    // 모임 기록 조회
    @GET("/moims/schedule/memo/{moimId}")
    suspend fun getMoimDiary(
        @Path("moimId") scheduleId: Long
    ): GetMoimDiaryResponse

    // 모임 메모 추가 or 수정
    @PATCH("/moims/schedule/memo/text/{scheduleId}")
    suspend fun patchMoimMemo(
        @Path("scheduleId") scheduleId: Long,
        @Body text: String?
    ): DiaryResponse

    // 모임 기록 활동 추가
    @Multipart
    @POST("/moims/schedule/memo/{moimId}")
    suspend fun addMoimDiary(
        @Path("moimId") scheduleId: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 수정
    @Multipart
    @PATCH("/moims/schedule/memo/{moimMemoLocationId}")
    suspend fun editMoimActivity(
        @Path("moimMemoLocationId") moimScheduldId: Long,
        @Part("name") place: RequestBody?,
        @Part("money") pay: RequestBody?,
        @Part("participants") member: RequestBody?,
        @Part imgs: List<MultipartBody.Part>?
    ): DiaryResponse

    // 모임 기록 활동 삭제
    @DELETE("/moims/schedule/memo/{moimMemoLocationId}")
    suspend fun deleteMoimActivity(
        @Path("moimMemoLocationId") moimActivityId: Long
    ): DiaryResponse


    // 모임 기록 월별 리스트 조회
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

