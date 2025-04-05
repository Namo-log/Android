package com.gradu.data.repositoriyImpl

import com.mongmong.namo.data.datasource.friend.RemoteFriendDataSource
import com.mongmong.namo.data.dto.FriendBaseResponse
import com.mongmong.namo.data.utils.mappers.FriendMapper.toModel
import com.mongmong.namo.domain.model.CalendarColorInfo
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.FriendRequest
import com.mongmong.namo.domain.model.FriendSchedule
import com.mongmong.namo.domain.repositories.FriendRepository
import com.mongmong.namo.domain.model.BaseResponse
import org.joda.time.DateTime
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
        startDate: DateTime,
        endDate: DateTime,
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