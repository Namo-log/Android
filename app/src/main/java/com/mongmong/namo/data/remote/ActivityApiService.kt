package com.mongmong.namo.data.remote

import com.mongmong.namo.data.dto.DiaryResponse
import com.mongmong.namo.data.dto.GetActivitiesResponse
import com.mongmong.namo.data.dto.GetActivityPaymentResponse
import com.mongmong.namo.data.dto.PatchActivityParticipantsRequest
import com.mongmong.namo.data.dto.PatchActivityPaymentRequest
import com.mongmong.namo.data.dto.PatchActivityRequest
import com.mongmong.namo.data.dto.PostActivityRequest
import com.mongmong.namo.domain.model.DiaryBaseResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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
    ): DiaryBaseResponse

    // 모임 활동 태그 수정
    @PATCH("activities/{activityId}/tag")
    suspend fun editActivityTag(
        @Path("activityId") activityId: Long,
        @Query("tag") tag: String
    ): DiaryBaseResponse

    // 모임 활동 정산 수정
    @PATCH("activities/{activityId}/settlement")
    suspend fun editActivityPayment(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityPaymentRequest
    ): DiaryBaseResponse

    // 모임 활동 참여자 수정
    @PATCH("activities/{activityId}/participants")
    suspend fun editActivityParticipants(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityParticipantsRequest
    ): DiaryBaseResponse

    // 모임 활동 수정
    @PATCH("activities/{activityId}/content")
    suspend fun editActivity(
        @Path("activityId") activityId: Long,
        @Body request: PatchActivityRequest
    ): DiaryBaseResponse

    // 모임 활동 삭제
    @DELETE("activities/{activityId}")
    suspend fun deleteActivity(
        @Path("activityId") activityId: Long
    ): DiaryBaseResponse
}