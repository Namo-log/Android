package com.example.namo.data.remote.moim

import com.example.namo.config.BaseResponse
import com.example.namo.data.entity.group.AddMoimSchedule
import com.example.namo.data.entity.group.EditMoimSchedule
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface MoimRetrofitInterface {

    // 모임 추가
    @Multipart
    @POST("moims")
    fun addMoim (
        @Part img : MultipartBody.Part?,
        @Part("groupName") groupName : RequestBody
    ) : Call<AddMoimResponse>

    // 모임 리스트 조회
    @GET("moims")
    fun getMoimList () : Call<GetMoimListResponse>


    // 월별 일정 조회
    @GET("moims/schedule/{moimId}/{yearMonth}")
    fun getMoimSchedule(
        @Path("moimId") moimId : Long,
        @Path("yearMonth") yearMonth : String
    ) : Call<GetMoimScheduleResponse>

    // 모임 이름 바꾸기
    @PATCH("moims/name")
    fun updateMoimName(
        @Body body: UpdateMoimNameBody
    ) : Call<AddMoimResponse>

    // 모임 참여하기
    @PATCH("moims/participate/{groupCode}")
    fun participateMoim(
        @Path("groupCode") groupCode : String
    ) : Call<ParticipateMoimResponse>

    // 모임 삭제하기
    @DELETE("moims/withdraw/{moimId}")
    fun deleteMoimMember(
        @Path("moimId") moimId : Long
    ) : Call<BaseResponse>

    // 모임 일정 추가하기
    @POST("moims/schedule")
    fun postMoimSchedule(
        @Body body : AddMoimSchedule
    ) : Call<AddMoimScheduleResponse>

    // 모임 일정 수정하기
    @PATCH("moims/schedule")
    fun editMoimSchedule(
        @Body body : EditMoimSchedule
    ) : Call<BaseResponse>

    // 모임 일정 삭제하기
    @DELETE("moims/schedule/{moimScheduleId}")
    fun deleteMoimSchedule(
        @Path("moimScheduleId") moimScheduleId : Long
    ) : Call<BaseResponse>

    @PATCH("moims/schedule/category")
    fun patchMoimScheduleCategory(
        @Body body : PatchMoimScheduleCategoryBody
    ) : Call<BaseResponse>

    @POST("moims/schedule/alarm")
    fun postMoimScheduleAlarm(
        @Body body : MoimScheduleAlarmBody
    ) : Call<BaseResponse>

    @PATCH("moims/schedule/alarm")
    fun patchMoimScheduleAlarm(
        @Body body : MoimScheduleAlarmBody
    ) : Call<BaseResponse>
}