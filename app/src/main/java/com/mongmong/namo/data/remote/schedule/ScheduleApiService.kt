package com.mongmong.namo.data.remote.schedule

import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ScheduleApiService {
    /** 개인 일정 */
    // 일정 생성
    @POST("schedules")
    suspend fun postSchedule(
        @Body schedule : ScheduleRequestBody
    ) : PostScheduleResponse

    // 일정 수정
    @PATCH("schedules/{serverId}")
    suspend fun editSchedule(
        @Path("serverId") serverId : Long,
        @Body schedule : ScheduleRequestBody
    ) : EditScheduleResponse

    // 일정 삭제
    @DELETE("schedules/{serverId}/{kind}")
    suspend fun deleteSchedule(
        @Path("serverId") serverId : Long,
        @Path("kind") isMoimSchedule: Int,
    ) : DeleteScheduleResponse

    // 월별 일정 조회
    @GET("schedules/{yearMonth}")
    fun getMonthSchedule(
        @Path("yearMonth") yearMonth : String,
    ) : Call<GetMonthScheduleResponse>

    // 일정 전체 조회
    @GET("schedules/all")
    fun getAllSchedule(
    ) : Call<GetMonthScheduleResponse>

    /** 모임 일정 */
    // 모임 일정 전체 조회
    @GET("schedules/moim/all")
    fun getAllMoimSchedule(
    ) : Call<GetMonthScheduleResponse>

    // 월별 모임 일정 조회
    @GET("schedules/moim/{yearMonth}")
    suspend fun getMonthMoimSchedule(
        @Path("yearMonth") yearMonth: String,
    ) : GetMonthScheduleResponse

    // 모임 일정 카테고리 수정
    @PATCH("moims/schedule/category")
    suspend fun patchMoimScheduleCategory(
        @Body body: PatchMoimScheduleCategoryRequestBody
    ): BaseResponse

    // 모임 일정 알림 수정
    @PATCH("moims/schedule/alarm")
    suspend fun patchMoimScheduleAlarm(
        @Body body: PatchMoimScheduleAlarmRequestBody
    ): BaseResponse
}