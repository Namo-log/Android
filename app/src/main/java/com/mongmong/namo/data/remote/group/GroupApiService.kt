package com.mongmong.namo.data.remote.group

import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.domain.model.group.AddGroupResponse
import com.mongmong.namo.domain.model.group.GetGroupsResponse
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.model.group.UpdateGroupNameRequestBody
import com.mongmong.namo.domain.model.group.UpdateGroupNameResponse
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
    @GET("groups")
    suspend fun getGroups(): GetGroupsResponse

    // 그룹 추가하기
    @Multipart
    @POST("groups")
    suspend fun addGroup(
        @Part img: MultipartBody.Part?,
        @Part("groupName") groupName: RequestBody
    ): AddGroupResponse

    // 그룹 참여하기
    @PATCH("groups/participate/{code}")
    suspend fun joinGroup(
        @Path("code") groupCode: String
    ): JoinGroupResponse

    // 그룹 탈퇴하기
    @DELETE("groups/withdraw/{groupId}")
    suspend fun deleteMember(
        @Path("groupId") groupId: Long
    ): BaseResponse

    // 그룹명 바꾸기
    @PATCH("groups/name")
    suspend fun updateGroupName(
        @Body body: UpdateGroupNameRequestBody
    ): UpdateGroupNameResponse
}