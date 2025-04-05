package com.gradu.domain.usecases.friend

import android.util.Log
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.repositories.FriendRepository
import javax.inject.Inject

open class GetFriendsUseCase @Inject constructor(private var friendRepository: FriendRepository) {
    suspend fun execute(): List<Friend> {
        Log.d("GetFriendsUseCase", "getCategories")
        return friendRepository.getFiendList()
    }
}