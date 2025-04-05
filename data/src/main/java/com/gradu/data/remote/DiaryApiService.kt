package com.gradu.data.remote

import com.mongmong.namo.data.dto.EditDiaryRequest
import com.mongmong.namo.data.dto.GetCalendarDiaryResponse
import com.mongmong.namo.data.dto.GetDiaryByDateResponse
import com.mongmong.namo.data.dto.GetDiaryArchiveResponse
import com.mongmong.namo.data.dto.GetDiaryResponse
import com.mongmong.namo.data.dto.GetMoimPaymentResponse
import com.mongmong.namo.data.dto.GetScheduleForDiaryResponse
import com.mongmong.namo.data.dto.PostDiaryRequest
import com.mongmong.namo.domain.model.BaseResponse
import retrofit2.http.*

interface DiaryApiService {
    /** 기록 */
    // 기록 보관함 조회
    @GET("diaries/archive")
    suspend fun getDiaryArchive(
        @Query("filterType") filterType: String?,
        @Query("keyword") keyword: String?,
        @Query("page") page: Int,
    ): GetDiaryArchiveResponse

    // 기록 일정 정보 조회
    @GET("schedules/{scheduleId}")
    suspend fun getScheduleForDiary(
        @Path("scheduleId") scheduleId: Long
    ): GetScheduleForDiaryResponse

    // 기록 개별 조회
    @GET("diaries/{scheduleId}")
    suspend fun getDiary(
        @Path("scheduleId") scheduleId: Long
    ): GetDiaryResponse

    // 기록 추가
    @POST("diaries")
    suspend fun addDiary(
        @Body requestBody: PostDiaryRequest
    ): BaseResponse

    // 기록 수정
    @PATCH("diaries/{diaryId}")
    suspend fun editDiary(
        @Path("diaryId") diaryId: Long,
        @Body requestBody: EditDiaryRequest
    ): BaseResponse

    // 기록 삭제
    @DELETE("diaries/{diaryId}")
    suspend fun deleteDiary(
        @Path("diaryId") diaryId: Long
    ): BaseResponse

    // 기록 캘린더 조회
    @GET("diaries/calendar/{yearMonth}")
    suspend fun getCalendarDiary(
        @Path("yearMonth") yearMonth: String
    ): GetCalendarDiaryResponse

    // 날짜별 기록 조회 (기록 캘린더)
    @GET("diaries/date/{date}")
    suspend fun getDiaryByDate(
        @Path("date") date: String
    ): GetDiaryByDateResponse

    @GET("schedules/meeting/{scheduleId}/settlement")
    suspend fun getMoimPayment(
        @Path("scheduleId") scheduleId: Long
    ): GetMoimPaymentResponse
}

