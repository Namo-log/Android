package com.gradu.data.remote

import com.mongmong.namo.data.dto.FriendBaseResponse
import com.mongmong.namo.data.dto.GetFriendCategoryResponse
import com.mongmong.namo.data.dto.GetFriendListResponse
import com.mongmong.namo.data.dto.GetFriendRequestResponse
import com.mongmong.namo.data.dto.GetFriendScheduleResponse
import com.mongmong.namo.domain.model.BaseResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendApiService {
    /** 친구 정보 */
    // 친구 목록 조회
    @GET("friends")
    suspend fun getFriendList(
        @Query("page") page: Int = 1,
        @Query("search") searchWord: String?
    ): GetFriendListResponse

    // 친구 캘린더 조회
    @GET("schedules/calendar/friends")
    suspend fun getFriendMonthSchedules(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("memberId") userId: Long
    ): GetFriendScheduleResponse

    // 친구 캘린더 카테고리 조회
    @GET("friends/{friendId}/categories")
    suspend fun getFriendCategories(
        @Path("friendId") userId: Long
    ): GetFriendCategoryResponse

    // 친구 삭제
    @DELETE("friends/{friendId}")
    suspend fun deleteFriend(
        @Path("friendId") userId: Long
    ): BaseResponse

    // 친구 즐겨찾기 등록/해제
    @PATCH("friends/{friendId}/toggle-favorite")
    suspend fun toggleFriendFavoriteState(
        @Path("friendId") userId: Long
    ): BaseResponse

    /** 친구 요청 */
    // 친구 요청 목록 조회
    @GET("friends/requests")
    suspend fun getFriendRequestList() : GetFriendRequestResponse

    // 친구 요청
    @POST("friends/{nickname-tag}")
    suspend fun postFriendRequest(
        @Path("nickname-tag") nicknameTag: String
    ): FriendBaseResponse

    // 친구 요청 수락
    @PATCH("friends/requests/{friendshipId}/accept")
    suspend fun acceptFriendRequest(
        @Path("friendshipId") friendRequestId: Long
    ): FriendBaseResponse

    // 친구 요청 거절
    @PATCH("friends/requests/{friendshipId}/reject")
    suspend fun rejectFriendRequest(
        @Path("friendshipId") friendRequestId: Long
    ): FriendBaseResponse
}