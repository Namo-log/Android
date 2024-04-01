package com.mongmong.namo.presentation.ui.bottom.group

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.AddGroupResult
import com.mongmong.namo.domain.model.Group
import com.mongmong.namo.domain.model.JoinGroupResponse
import com.mongmong.namo.domain.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepository
): ViewModel() {
    private val _groups = MutableLiveData<List<Group>>()
    val groups : LiveData<List<Group>> = _groups

    private val _addGroupResult = MutableLiveData<AddGroupResult>()
    val addGroupResult: LiveData<AddGroupResult> = _addGroupResult

    private val _joinGroupResult = MutableLiveData<JoinGroupResponse>()
    val joinGroupResult: LiveData<JoinGroupResponse> = _joinGroupResult

    fun getGroups() {
        viewModelScope.launch {
            _groups.postValue(repository.getGroups())
        }
    }

    fun addGroup(img: Uri, name: String) {
        viewModelScope.launch {
            _addGroupResult.postValue(repository.addGroups(img, name))
        }
    }

    fun joinGroup(groupCode: String) {
        viewModelScope.launch {
            _joinGroupResult.postValue(repository.joinGroup(groupCode))
        }
    }
}