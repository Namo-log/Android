package com.mongmong.namo.data.remote.group

import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.domain.model.group.AddGroupResponse
import com.mongmong.namo.domain.model.group.GetGroupsResponse
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.model.group.UpdateGroupNameRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface GroupApiService {
    // 그룹 리스트 조회
    @GET("moims")
    suspend fun getGroups(): GetGroupsResponse

    // 그룹 추가하기
    @Multipart
    @POST("moims")
    suspend fun addGroup(
        @Part img: MultipartBody.Part?,
        @Part("groupName") groupName: RequestBody
    ): AddGroupResponse

    // 그룹 참여하기
    @PATCH("moims/participate/{groupCode}")
    suspend fun joinGroup(
        @Path("groupCode") groupCode: String
    ): JoinGroupResponse

    // 그룹 탈퇴하기
    @DELETE("moims/withdraw/{moimId}")
    suspend fun deleteMember(
        @Path("moimId") groupId: Long
    ): BaseResponse

    // 그룹명 바꾸기
    @PATCH("moims/name")
    suspend fun updateGroupName(
        @Body body: UpdateGroupNameRequestBody
    ): JoinGroupResponse
}