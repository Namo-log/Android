package com.mongmong.namo.data.remote.schedule

import com.mongmong.namo.data.local.entity.home.ScheduleForUpload
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.presentation.config.BaseResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ScheduleRetrofitInterface {
    @POST("schedules")
    suspend fun postSchedule(
        @Body schedule : ScheduleForUpload
    ) : PostScheduleResponse

    @PATCH("schedules/{serverId}")
    suspend fun editSchedule(
        @Path("serverId") serverId : Long,
        @Body schedule : ScheduleForUpload
    ) : EditScheduleResponse

    @DELETE("schedules/{serverId}/{kind}")
    suspend fun deleteSchedule(
        @Path("serverId") serverId : Long,
        @Path("kind") isMoimSchedule: Int,
    ) : DeleteScheduleResponse

    @GET("schedules/{yearMonth}")
    fun getMonthSchedule(
        @Path("yearMonth") yearMonth : String,
    ) : Call<GetMonthScheduleResponse>

    @GET("schedules/all")
    fun getAllSchedule(
    ) : Call<GetMonthScheduleResponse>

    @GET("schedules/moim/all")
    fun getAllMoimSchedule(
    ) : Call<GetMonthScheduleResponse>

    @GET("schedules/moim/{yearMonth}")
    suspend fun getMonthMoimSchedule(
        @Path("yearMonth") yearMonth: String,
    ) : GetMonthScheduleResponse

    @PATCH("moims/schedule/category")
    suspend fun patchMoimScheduleCategory(
        @Body body: PatchMoimScheduleCategoryBody
    ): BaseResponse
}