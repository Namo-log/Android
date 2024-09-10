package com.mongmong.namo.presentation.ui.community.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Friend

class FriendViewModel: ViewModel() {
    private val _friendList = MutableLiveData<List<Friend>>(emptyList())
    val friendList: LiveData<List<Friend>> = _friendList

    val friend = Friend(
        0, "https://github.com/nahy-512/nahy-512/assets/101113025/3fb8e968-e482-4aff-9334-60c41014a80f",
        "코코아", "#1111", "한줄 소개입니다", "김나현", "1월 25일", true
    )

    init {
        _friendList.value = listOf(
            friend
        )
    }
}