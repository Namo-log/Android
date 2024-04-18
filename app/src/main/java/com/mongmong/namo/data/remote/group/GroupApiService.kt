package com.mongmong.namo.data.remote.group

import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.domain.model.AddGroupResponse
import com.mongmong.namo.domain.model.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.GetGroupsResponse
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.JoinGroupResponse
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.model.UpdateGroupNameRequest
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

interface GroupApiService {

    /** 그룹 */
    // 그룹 추가
    @Multipart
    @POST("moims")
    suspend fun addGroup(
        @Part img: MultipartBody.Part?,
        @Part("groupName") groupName: RequestBody
    ): AddGroupResponse

    // 그룹 리스트 조회
    @GET("moims")
    suspend fun getGroups(): GetGroupsResponse

    // 그룹명 바꾸기
    @PATCH("moims/name")
    suspend fun updateGroupName(
        @Body body: UpdateGroupNameRequest
    ): JoinGroupResponse

    // 그룹 참여하기
    @PATCH("moims/participate/{groupCode}")
    suspend fun joinGroup(
        @Path("groupCode") groupCode: String
    ): JoinGroupResponse

    // 그룹 삭제하기
    @DELETE("moims/withdraw/{moimId}")
    suspend fun deleteMember(
        @Path("moimId") groupId: Long
    ): BaseResponse

    /** 모임 */
    // 월별 일정 조회
    @GET("moims/schedule/{moimId}/{yearMonth}")
    fun getMonthMoimSchedule(
        @Path("moimId") moimId: Long,
        @Path("yearMonth") yearMonth: String
    ): Call<GetMoimScheduleResponse>

    // 모든 일정 조회
    @GET("moims/schedule/{moimId}/all")
    suspend fun getAllMoimSchedule(
        @Path("moimId") moimId: Long
    ): GetMoimScheduleResponse

    // 모임 일정 추가하기
    @POST("moims/schedule")
    suspend fun postMoimSchedule(
        @Body body: AddMoimSchedule
    ): AddMoimScheduleResponse

    // 모임 일정 수정하기
    @PATCH("moims/schedule")
    suspend fun editMoimSchedule(
        @Body body: EditMoimSchedule
    ): BaseResponse

    // 모임 일정 삭제하기
    @DELETE("moims/schedule/{moimScheduleId}")
    suspend fun deleteMoimSchedule(
        @Path("moimScheduleId") moimScheduleId: Long
    ): BaseResponse
}