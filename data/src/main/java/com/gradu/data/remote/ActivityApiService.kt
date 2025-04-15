package com.gradu.data.remote

import com.gradu.data.dto.GetActivitiesResponse
import com.gradu.data.dto.GetActivityPaymentResponse
import com.gradu.data.dto.PatchActivityParticipantsRequest
import com.gradu.data.dto.PatchActivityPaymentRequest
import com.gradu.data.dto.PatchActivityRequest
import com.gradu.data.dto.PostActivityRequest
import com.gradu.domain.model.BaseResponse
import retrofit2.http.*


interface ActivityApiService {
    /** 활동 */
    // 모임 기록 활동 리스트 조회
    @GET("activities/{scheduleId}")
    suspend fun getActivities(
        @Path("scheduleId") scheduleId: Long
    ): GetActivitiesResponse

    // 활동 정산 조회
    @GET("activities/{activityId}/settlement")
    suspend fun getActivityPayment(
        @Path("activityId") activityId: Long
    ): GetActivityPaymentResponse

    // 모임 활동 추가
    @POST("activities/{scheduleId}")
    suspend fun addActivity(
        @Path("scheduleId") scheduleId: Long,
        @Body request: PostActivityRequest
    ): BaseResponse

    // 모임 활동 태그 수정
    @PATCH("activities/{activityId}/tag")
    suspend fun editActivityTag(
        @Path("activityId") activityId: Long,
        @Query("tag") tag: String
    ): BaseResponse

    // 모임 활동 정산 수정
    @PATCH("activities/{activityId}/settlement")
    suspend fun editActivityPayment(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityPaymentRequest
    ): BaseResponse

    // 모임 활동 참여자 수정
    @PATCH("activities/{activityId}/participants")
    suspend fun editActivityParticipants(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityParticipantsRequest
    ): BaseResponse

    // 모임 활동 수정
    @PATCH("activities/{activityId}/content")
    suspend fun editActivity(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityRequest
    ): BaseResponse

    // 모임 활동 삭제
    @DELETE("activities/{activityId}")
    suspend fun deleteActivity(
        @Path("activityId") activityId: Long
    ): BaseResponse
}