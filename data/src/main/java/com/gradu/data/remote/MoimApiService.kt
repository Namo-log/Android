package com.gradu.data.remote

import com.gradu.data.dto.EditMoimScheduleProfileRequestBody
import com.gradu.data.dto.EditMoimScheduleRequestBody
import com.gradu.data.dto.GetMoimCalendarResponse
import com.gradu.data.dto.GetMoimDetailResponse
import com.gradu.data.dto.GetMoimResponse
import com.gradu.data.dto.InviteMoimParticipantRequestBody
import com.gradu.data.dto.MoimBaseResponse
import com.gradu.data.dto.MoimScheduleRequestBody
import com.gradu.data.dto.PostMoimScheduleResponse
import com.gradu.domain.model.BaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MoimApiService {
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

    // 모임 일정 수정 (방장 only)
    @PATCH("schedules/meeting/{meetingScheduleId}")
    suspend fun editMoimSchedule(
        @Path("meetingScheduleId") moimScheduleId: Long,
        @Body body: EditMoimScheduleRequestBody
    ): BaseResponse

    // 모임 일정 삭제 (나가기)
    @DELETE("schedules/meeting/{meetingScheduleId}/withdraw")
    suspend fun deleteMoimSchedule(
        @Path("meetingScheduleId") moimScheduleId: Long
    ): BaseResponse

    // 모임 일정 프로필 수정
    @PATCH("schedules/meeting/{meetingScheduleId}/profile")
    suspend fun editMoimScheduleProfile(
        @Path("meetingScheduleId") moimScheduleId: Long,
        @Body body: EditMoimScheduleProfileRequestBody
    ): BaseResponse

    // 모임 일정 참석자 초대
    @POST("schedules/meeting/{meetingScheduleId}/invitations")
    suspend fun inviteMoimParticipants(
        @Path("meetingScheduleId") moimScheduleId: Long,
        @Body body: InviteMoimParticipantRequestBody
    ): BaseResponse

    // 게스트 초대용 링크 조회
    @GET("schedules/meeting/{meetingScheduleId}/invitations")
    suspend fun getGuestInvitationLink(
        @Path("meetingScheduleId") moimScheduleId: Long,
    ): MoimBaseResponse
}