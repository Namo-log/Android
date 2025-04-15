package com.gradu.data.repositoriyImpl

import com.gradu.data.datasource.friend.RemoteFriendDataSource
import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.CalendarColorInfo
import com.gradu.domain.model.Friend
import com.gradu.domain.model.FriendBaseResponse
import com.gradu.domain.model.FriendRequest
import com.gradu.domain.model.FriendSchedule
import com.gradu.domain.repositories.FriendRepository
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val remoteFriendDataSource: RemoteFriendDataSource
): FriendRepository {
    override suspend fun getFiendList(): List<Friend> {
        return remoteFriendDataSource.getFriends().result.friendList.map { friend ->
            friend.toModel()
        }
    }

    override suspend fun getFriendCalendar(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        userId: Long
    ): List<FriendSchedule> {
        return remoteFriendDataSource.getFriendMonthSchedules(startDate, endDate, userId).result.map { schedule ->
            schedule.toModel()
        }
    }

    override suspend fun getFriendCategoryList(userId: Long): List<CalendarColorInfo> {
        return remoteFriendDataSource.getFriendCategories(userId).result.map { category ->
            category.toModel()
        }
    }

    override suspend fun deleteFriend(userId: Long): BaseResponse {
        return remoteFriendDataSource.deleteFriend(userId)
    }

    override suspend fun toggleFriendFavoriteState(userId: Long): BaseResponse {
        return remoteFriendDataSource.toggleFriendFavoriteState(userId)
    }

    override suspend fun getFriendRequests(): List<FriendRequest> {
        return remoteFriendDataSource.getFriendRequests().result.friendRequests.map { friendRequest ->
            friendRequest.toModel()
        }
    }

    override suspend fun doFriendRequest(nicknameTag: String): FriendBaseResponse {
        return remoteFriendDataSource.postFriendRequest(nicknameTag)
    }

    override suspend fun acceptFriendRequest(requestId: Long): FriendBaseResponse {
        return remoteFriendDataSource.acceptFriendRequest(requestId)
    }

    override suspend fun rejectFriendRequest(requestId: Long): FriendBaseResponse {
        return remoteFriendDataSource.rejectFriendRequest(requestId)
    }
}