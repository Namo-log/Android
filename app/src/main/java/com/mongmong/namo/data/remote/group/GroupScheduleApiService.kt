package com.mongmong.namo.data.remote.group

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
    // 그룹의 모든 일정 조회
    @GET("moims/schedule/{moimId}/all")
    suspend fun getAllMoimSchedule(
        @Path("moimId") moimId: Long
    ): GetMoimScheduleResponse

    // 그룹의 월별 일정 조회
    @GET("moims/schedule/{moimId}/{yearMonth}")
    fun getMonthMoimSchedule(
        @Path("moimId") moimId: Long,
        @Path("yearMonth") yearMonth: String
    ): Call<GetMoimScheduleResponse>

    // 모임 일정 추가하기
    @POST("moims/schedule")
    suspend fun postMoimSchedule(
        @Body body: AddMoimScheduleRequestBody
    ): AddMoimScheduleResponse

    // 모임 일정 수정하기
    @PATCH("moims/schedule")
    suspend fun editMoimSchedule(
        @Body body: EditMoimScheduleRequestBody
    ): BaseResponse

    // 모임 일정 삭제하기
    @DELETE("moims/schedule/{moimScheduleId}")
    suspend fun deleteMoimSchedule(
        @Path("moimScheduleId") moimScheduleId: Long
    ): BaseResponse
}