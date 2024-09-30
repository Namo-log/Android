package com.mongmong.namo.data.remote

import com.mongmong.namo.data.dto.DeleteScheduleResponse
import com.mongmong.namo.data.dto.GetMonthScheduleResponse
import com.mongmong.namo.data.dto.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.data.dto.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.data.dto.PostScheduleResponse
import com.mongmong.namo.data.dto.EditScheduleResponse
import com.mongmong.namo.data.dto.GetMoimDetailResponse
import com.mongmong.namo.data.dto.GetMoimResponse
import com.mongmong.namo.data.dto.ScheduleRequestBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApiService {
    /** 개인 일정 */
    // 한달 날짜 범위로 일정 조회
    @GET("schedules/calendar")
    suspend fun getMonthSchedule(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ) : GetMonthScheduleResponse

    // 일정 생성
    @POST("schedules")
    suspend fun postSchedule(
        @Body schedule : ScheduleRequestBody
    ) : PostScheduleResponse

    // 일정 수정
    @PATCH("schedules/{scheduleId}")
    suspend fun editSchedule(
        @Path("scheduleId") serverId : Long,
        @Body schedule : ScheduleRequestBody
    ) : EditScheduleResponse

    // 일정 삭제
    @DELETE("schedules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("scheduleId") serverId : Long
    ) : DeleteScheduleResponse

    /** 모임 일정 */
    // 월별 모임 일정 조회
    @GET("schedules/group/{yearMonth}")
    suspend fun getMonthMoimSchedule(
        @Path("yearMonth") yearMonth: String,
    ) : GetMonthScheduleResponse

    // 모임 일정 카테고리 수정
    @PATCH("group/schedules/category")
    suspend fun patchMoimScheduleCategory(
        @Body body: PatchMoimScheduleCategoryRequestBody
    ): BaseResponse

    // 모임 일정 알림 수정
    @PATCH("group/schedules/alarm")
    suspend fun patchMoimScheduleAlarm(
        @Body body: PatchMoimScheduleAlarmRequestBody
    ): BaseResponse
}