package com.gradu.domain.usecases.friend

import com.gradu.domain.model.Friend
import com.gradu.domain.repositories.FriendRepository
import javax.inject.Inject

open class GetFriendsUseCase @Inject constructor(private var friendRepository: FriendRepository) {
    suspend fun execute(): List<Friend> {
        return friendRepository.getFiendList()
    }
}