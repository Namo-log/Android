package com.mongmong.namo.presentation.ui.community.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Friend

class FriendViewModel: ViewModel() {
    private val _friendList = MutableLiveData<List<Friend>>(emptyList())
    val friendList: LiveData<List<Friend>> = _friendList

    init {
        _friendList.value = listOf(
            Friend(
              0, "", "코코아", "한줄 소개입니다", true
            )
        )
    }
}