package com.mongmong.namo.presentation.ui.community.moim.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Friend

class FriendInviteViewModel: ViewModel() {
    //TODO: 임시 데이터
    private val _friendList = MutableLiveData<List<Friend>>()
    val friendList: LiveData<List<Friend>> = _friendList

    init {
        _friendList.value = listOf(
            Friend(0, "https://github.com/nahy-512/nahy-512/assets/101113025/3fb8e968-e482-4aff-9334-60c41014a80f", "코코아", "", true),
            Friend(0, "", "구짱", "별명은 방구짱입니다.", false),
        )
    }
}