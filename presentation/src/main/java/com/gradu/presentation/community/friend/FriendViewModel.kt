package com.gradu.presentation.ui.community.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.repositories.FriendRepository
import com.mongmong.namo.domain.usecases.friend.AcceptFriendRequestUseCase
import com.mongmong.namo.domain.usecases.friend.DenyFriendRequestUseCase
import com.mongmong.namo.domain.usecases.friend.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: FriendRepository,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val denyFriendRequestUseCase: DenyFriendRequestUseCase
): ViewModel() {
    private val _friendList = MutableLiveData<List<Friend>>(emptyList())
    val friendList: LiveData<List<Friend>> = _friendList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    /** 친구 목록 조회 */
     fun getFriends() {
        viewModelScope.launch {
            _friendList.value = getFriendsUseCase.execute()
        }
    }

    /** 친구 삭제 */
    fun deleteFriend(userId: Long) {
        viewModelScope.launch {
            _isComplete.value = repository.deleteFriend(userId).isSuccess
        }
    }

    /** 친구 즐겨찾기 여부 변경 */
    fun toggleFriendFavoriteState(userId: Long) {
        viewModelScope.launch {
            _isComplete.value = repository.toggleFriendFavoriteState(userId).isSuccess
        }
    }

    /** 친구 신청 */
    fun requestFriend(nicknameTag: String) {
        viewModelScope.launch {
            _isComplete.value = repository.doFriendRequest(nicknameTag).isSuccess
        }
    }

    /** 친구 요청 수락 */
    fun acceptFriendRequest(requestId: Long) {
        viewModelScope.launch {
            _isComplete.value = acceptFriendRequestUseCase.execute(requestId).isSuccess
        }
    }

    /** 친구 요청 거절 */
    fun denyFriendRequest(requestId: Long) {
        viewModelScope.launch {
            _isComplete.value = denyFriendRequestUseCase.execute(requestId).isSuccess
        }
    }
}