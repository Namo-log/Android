package com.gradu.domain.usecases.friend

import com.gradu.domain.model.FriendBaseResponse
import com.gradu.domain.repositories.FriendRepository
import javax.inject.Inject

class DenyFriendRequestUseCase @Inject constructor(private val friendRepository: FriendRepository) {
    suspend fun execute(
        friendRequestId: Long
    ): FriendBaseResponse {
        return friendRepository.rejectFriendRequest(friendRequestId)
    }
}