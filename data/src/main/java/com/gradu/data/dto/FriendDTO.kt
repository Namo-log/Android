package com.gradu.data.dto

import com.mongmong.namo.domain.model.BaseResponse
import java.time.LocalDateTime

data class FriendBaseResponse(
    val result: String = ""
): BaseResponse()

/** 친구 목록 조회 */
data class GetFriendListResponse(
    val result: GetFriendListResult
)

data class GetFriendListResult(
    var friendList: List<FriendDTO> = emptyList(),
    var totalPage: Int = 0,
    var currentPage: Int = 0,
    var pageSize: Int = 0,
    var totalItems: Int = 0
)

data class FriendDTO(
    var memberId: Long = 0L,
    var nickname: String = "",
    var favoriteFriend: Boolean = false,
    var profileImage: String? = "",
    var tag: String = "",
    var bio: String = "",
    var birthDay: String = "",
    var favoriteColorId: Int = 0
)

/** 친구의 일정 조회 */
data class GetFriendScheduleResponse(
    val result: List<GetFriendScheduleResult>
)

data class GetFriendScheduleResult(
    var scheduleId: Long = 0L,
    var title: String = "",
    var categoryInfo: ScheduleCategoryInfo,
    var startDate: String = LocalDateTime.now().toString(),
    var endDate: String = LocalDateTime.now().toString(),
    var scheduleType: Int = 0
)

/** 친구 일정 카테고리 조회 */
data class GetFriendCategoryResponse(
    val result: List<FriendCategoryDTO>
): BaseResponse()

data class FriendCategoryDTO(
    val colorId: Int = 1,
    val categoryName: String = ""
)

/** 친구 요청 목록 조회 */
data class GetFriendRequestResponse(
    val result: GetFriendRequestResult
): BaseResponse()

data class GetFriendRequestResult(
    var friendRequests: List<FriendRequestDTO> = emptyList(),
    var totalPage: Int = 0,
    var currentPage: Int = 0,
    var pageSize: Int = 0,
    var totalItems: Int = 0
)

data class FriendRequestDTO(
    var memberId: Long = 0L,
    var friendRequestId: Long = 0L,
    var profileImage: String = "",
    var nickname: String = "",
    var tag: String = "",
    var bio: String = "",
    var birth: String = "",
    var favoriteColorId: Int = 0
)