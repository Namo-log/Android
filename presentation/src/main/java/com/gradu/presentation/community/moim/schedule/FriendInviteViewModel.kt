package com.gradu.presentation.community.moim.schedule

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.Friend
import com.gradu.domain.repositories.ScheduleRepository
import com.gradu.domain.usecases.friend.GetFriendsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendInviteViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val getFriendsUseCase: GetFriendsUseCase,
): ViewModel() {
    var moimScheduleId: Long = 0L

    var invitedUserIdList: List<Long>? = null

    // 모든 친구 목록
    private val _allFriendList = MutableLiveData<List<Friend>>()
    val allFriendList: LiveData<List<Friend>> = _allFriendList

    // allFriend - invitedFriend
    private val _remainFriendList = MutableLiveData<List<Friend>>()
    val remainFriendList: LiveData<List<Friend>> = _remainFriendList

    // 이미 초대된 친구
    var invitedFriendList: List<Friend> = emptyList()

    // 초대할 친구 목록
    private val _friendToInviteList = MutableLiveData<ArrayList<Friend>>(ArrayList())
    val friendToInviteList: LiveData<ArrayList<Friend>> = _friendToInviteList

    // API 호출 성공 여부
    private val _friendInviteResult = MutableLiveData<BaseResponse>()
    var friendInviteResult: LiveData<BaseResponse> = _friendInviteResult

    val isInvitedFriendVisible = MutableLiveData<Boolean>(true)

    init {
        getFriends()
    }

    /** 친구 목록 조회 */
    private fun getFriends() {
        viewModelScope.launch {
            _allFriendList.value = getFriendsUseCase.execute()
        }
    }

    /** 모임 일정 참석자 초대 */
    fun inviteMoimParticipants() {
        Log.d("FriendInviteVM", "moimScheduleId: $moimScheduleId")
        if (moimScheduleId == 0L) return
        viewModelScope.launch {
            _friendInviteResult.value = repository.inviteMoimParticipant(moimScheduleId, _friendToInviteList.value!!.map { friend -> friend.userId })
        }
    }

    // 초대된 친구 세팅
    fun setInvitedFriend() {
        if (invitedUserIdList.isNullOrEmpty()) return

        // 초대된 친구
        invitedFriendList = _allFriendList.value?.filter {
            it.userId in invitedUserIdList!!
        } ?: emptyList()

        // 초대되고 남은 친구
        _remainFriendList.value = _allFriendList.value?.filter {
            it.userId !in invitedUserIdList!!
        }
    }

    // 초대할 친구 선택 초기화
    fun resetAllSelectedFriend() {
        _friendToInviteList.value = ArrayList()
    }

    // 친구 초대 상태 변경
    fun updateSelectedFriend(isSelected: Boolean, friend: Friend) {
        val tempFriendArr =  _friendToInviteList.value!!
        if (isSelected) tempFriendArr.add(friend)
        else tempFriendArr.remove(friend)
        _friendToInviteList.value = tempFriendArr
    }

    fun toggleInvitedFriendVisibility() {
        isInvitedFriendVisible.value = !isInvitedFriendVisible.value!!
    }
}