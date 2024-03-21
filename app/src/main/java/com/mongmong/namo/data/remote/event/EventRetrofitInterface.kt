package com.mongmong.namo.data.remote.event

import com.mongmong.namo.data.local.entity.home.EventForUpload
import com.mongmong.namo.domain.model.DeleteEventResponse
import com.mongmong.namo.domain.model.EditEventResponse
import com.mongmong.namo.domain.model.GetMonthEventResponse
import com.mongmong.namo.domain.model.PostEventResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface EventRetrofitInterface {
    @POST("schedules")
    fun postEvent(
        @Body event : EventForUpload
    ) : PostEventResponse

    @PATCH("schedules/{serverIdx}")
    fun editEvent(
        @Path("serverIdx") serverIdx : Long,
        @Body event : EventForUpload
    ) : EditEventResponse

    @DELETE("schedules/{serverIdx}/{kind}")
    fun deleteEvent(
        @Path("serverIdx") serverIdx : Long,
        @Path("kind") isMoimSchedule: Int,
//        @Header("Authorization") token : String,
    ) : Call<DeleteEventResponse>

    @GET("schedules/{yearMonth}")
    fun getMonthEvent(
        @Path("yearMonth") yearMonth : String,
    ) : Call<GetMonthEventResponse>

    @GET("schedules/all")
    fun getAllEvent(
    ) : Call<GetMonthEventResponse>

    @GET("schedules/moim/all")
    fun getAllMoimEvent(
    ) : Call<GetMonthEventResponse>

    @GET("schedules/moim/{yearMonth}")
    fun getMonthMoimEvent(
        @Path("yearMonth") yearMonth: String,
    ) : Call<GetMonthEventResponse>
}