package com.gradu.domain.repositories

import com.mongmong.namo.data.dto.FriendBaseResponse
import com.mongmong.namo.domain.model.CalendarColorInfo
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.FriendRequest
import com.mongmong.namo.domain.model.FriendSchedule
import com.mongmong.namo.domain.model.BaseResponse
import org.joda.time.DateTime

interface FriendRepository {
    suspend fun getFiendList(): List<Friend>

    suspend fun getFriendCalendar(
        startDate: DateTime,
        endDate: DateTime,
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