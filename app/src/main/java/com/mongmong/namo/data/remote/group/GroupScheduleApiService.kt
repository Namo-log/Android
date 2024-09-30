package com.mongmong.namo.data.remote.group

import com.mongmong.namo.data.dto.GetMoimDetailResponse
import com.mongmong.namo.data.dto.GetMoimResponse
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.GetMoimScheduleResponse
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface GroupScheduleApiService {
    // 모임 일정 목록 조회
    @GET("schedules/meeting")
    suspend fun getAllMoimSchedule(): GetMoimResponse

    // 모임 일정 상세 조회
    @GET("schedules/meeting/{meetingScheduleId}")
    suspend fun getMoimScheduleDetail(
        @Path("meetingScheduleId") moimScheduleId: Long
    ) : GetMoimDetailResponse

    // 그룹의 모든 일정 조회
    @GET("group/schedules/{groupId}/all")
    suspend fun getAllMoimSchedule(
        @Path("groupId") moimId: Long
    ): GetMoimScheduleResponse

    // 그룹의 월별 일정 조회
    @GET("group/schedules/{groupId}/{yearMonth}")
    fun getMonthMoimSchedule(
        @Path("groupId") moimId: Long,
        @Path("yearMonth") yearMonth: String
    ): Call<GetMoimScheduleResponse>

    // 모임 일정 생성
    @POST("group/schedules")
    suspend fun postMoimSchedule(
        @Body body: AddMoimScheduleRequestBody
    ): AddMoimScheduleResponse

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