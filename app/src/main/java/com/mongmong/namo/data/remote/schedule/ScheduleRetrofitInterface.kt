package com.mongmong.namo.data.remote.schedule

import com.mongmong.namo.data.local.entity.home.ScheduleForUpload
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.PostScheduleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ScheduleRetrofitInterface {
    @POST("schedules")
    fun postSchedule(
        @Body schedule : ScheduleForUpload
    ) : PostScheduleResponse

    @PATCH("schedules/{serverIdx}")
    fun editSchedule(
        @Path("serverIdx") serverId : Long,
        @Body schedule : ScheduleForUpload
    ) : EditScheduleResponse

    @DELETE("schedules/{serverIdx}/{kind}")
    fun deleteSchedule(
        @Path("serverIdx") serverId : Long,
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
    fun getMonthMoimSchedule(
        @Path("yearMonth") yearMonth: String,
    ) : Call<GetMonthScheduleResponse>
}