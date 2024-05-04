package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.group.GetMoimDiaryResponse
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface DiaryApiService {
    /** 개인 */
    // 개인 기록 전체 조회
    @GET("/schedules/diary/all")
    fun getAllDiary(): Call<DiaryGetAllResponse>

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

    /** 모임 */
    // 모임 기록 개별 조회
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

}

