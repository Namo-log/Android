package com.mongmong.namo.presentation.ui.community.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.FriendRequest
import com.mongmong.namo.domain.repositories.FriendRepository
import com.mongmong.namo.domain.usecases.friend.AcceptFriendRequestUseCase
import com.mongmong.namo.domain.usecases.friend.DenyFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val denyFriendRequestUseCase: DenyFriendRequestUseCase
): ViewModel() {

    private val _friendRequestList = MutableLiveData<List<FriendRequest>>(emptyList())
    val friendRequestList: LiveData<List<FriendRequest>> = _friendRequestList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    init {
        getFriendRequests()
    }

    /** 친구 요청 목록 조회 */
    fun getFriendRequests() {
        viewModelScope.launch {
            _friendRequestList.value = friendRepository.getFriendRequests()
        }
    }

    /** 친구 요청 수락 */
    fun acceptFriendRequest(requestId: Long) {
        viewModelScope.launch {
            _isComplete.value = acceptFriendRequestUseCase.execute(requestId).isSuccess
            if (_isComplete.value == true) getFriendRequests()
        }
    }

    /** 친구 요청 거절 */
    fun denyFriendRequest(requestId: Long) {
        viewModelScope.launch {
            _isComplete.value = denyFriendRequestUseCase.execute(requestId).isSuccess
            if (_isComplete.value == true) getFriendRequests()
        }
    }
}