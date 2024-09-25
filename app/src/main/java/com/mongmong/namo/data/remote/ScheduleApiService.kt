package com.mongmong.namo.data.remote

import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.domain.model.ScheduleDefaultResponse
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApiService {
    /** 개인 일정 */
    // 월별 일정 조회
    @GET("schedules/calendar")
    suspend fun getMonthSchedule(
        @Query("year") yaer: Int,
        @Query("month") month: Int
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
    ) : ScheduleDefaultResponse

    // 일정 삭제
    @DELETE("schedules/{scheduleId}/{kind}")
    suspend fun deleteSchedule(
        @Path("scheduleId") serverId : Long,
        @Path("kind") isMoimSchedule: Int,
    ) : DeleteScheduleResponse

    /** 모임 일정 */
    // 모임 일정 전체 조회
    @GET("schedules/group/all")
    fun getAllMoimSchedule(
    ) : Call<GetMonthScheduleResponse>

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