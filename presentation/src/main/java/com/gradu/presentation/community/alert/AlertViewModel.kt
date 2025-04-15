package com.gradu.presentation.community.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gradu.domain.model.FriendRequest
import com.gradu.domain.repositories.FriendRepository
import com.gradu.domain.usecases.friend.AcceptFriendRequestUseCase
import com.gradu.domain.usecases.friend.DenyFriendRequestUseCase
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