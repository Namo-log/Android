package com.gradu.domain.usecases.friend

import com.mongmong.namo.data.dto.FriendBaseResponse
import com.mongmong.namo.domain.repositories.FriendRepository
import javax.inject.Inject

class DenyFriendRequestUseCase @Inject constructor(private val friendRepository: FriendRepository) {
    suspend fun execute(
        friendRequestId: Long
    ): FriendBaseResponse {
        return friendRepository.rejectFriendRequest(friendRequestId)
    }
}