package com.mongmong.namo.data.remote.group

import com.mongmong.namo.data.dto.GetMoimCalendarResponse
import com.mongmong.namo.data.dto.GetMoimDetailResponse
import com.mongmong.namo.data.dto.GetMoimResponse
import com.mongmong.namo.data.dto.MoimScheduleRequestBody
import com.mongmong.namo.data.dto.PostMoimScheduleResponse
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GroupScheduleApiService {
    // 모임 일정 목록 조회
    @GET("schedules/meeting")
    suspend fun getMoimCalendarSchedule(): GetMoimResponse

    // 모임 일정 상세 조회
    @GET("schedules/meeting/{meetingScheduleId}")
    suspend fun getMoimScheduleDetail(
        @Path("meetingScheduleId") moimScheduleId: Long
    ) : GetMoimDetailResponse

    // 모임 캘린더 조회
    @GET("schedules/meeting/{meetingScheduleId}/calendar")
    suspend fun getMoimCalendarSchedule(
        @Path("meetingScheduleId") moimId: Long,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): GetMoimCalendarResponse

    // 모임 일정 생성
    @POST("schedules/meeting")
    suspend fun postMoimSchedule(
        @Body body: MoimScheduleRequestBody
    ): PostMoimScheduleResponse

    // 모임 일정 수정
    @PATCH("group/schedules")
    suspend fun editMoimSchedule(
        @Body body: EditMoimScheduleRequestBody
    ): BaseResponse

    // 모임 일정 삭제
    @DELETE("group/schedules/{moimScheduleId}")
    suspend fun deleteMoimSchedule(
        @Path("moimScheduleId") moimScheduleId: Long
    ): BaseResponse
}