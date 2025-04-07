package com.gradu.domain.repositories

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.CalendarColorInfo
import com.gradu.domain.model.Friend
import com.gradu.domain.model.FriendBaseResponse
import com.gradu.domain.model.FriendRequest
import com.gradu.domain.model.FriendSchedule
import kotlinx.datetime.LocalDateTime

interface FriendRepository {
    suspend fun getFiendList(): List<Friend>

    suspend fun getFriendCalendar(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        userId: Long
    ): List<FriendSchedule>

    suspend fun getFriendCategoryList(
        userId: Long
    ): List<CalendarColorInfo>

    suspend fun deleteFriend(
        userId: Long
    ): BaseResponse

    suspend fun toggleFriendFavoriteState(
        userId: Long
    ): BaseResponse

    suspend fun getFriendRequests(): List<FriendRequest>

    suspend fun doFriendRequest(
        nicknameTag: String
    ): FriendBaseResponse

    suspend fun acceptFriendRequest(
        requestId: Long
    ): FriendBaseResponse

    suspend fun rejectFriendRequest(
        requestId: Long
    ): FriendBaseResponse
}