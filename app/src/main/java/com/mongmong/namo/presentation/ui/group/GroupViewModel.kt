package com.mongmong.namo.presentation.ui.group

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.group.AddGroupResult
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.repositories.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: GroupRepository
) : ViewModel() {
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    private val _addGroupResult = MutableLiveData<AddGroupResult>()
    val addGroupResult: LiveData<AddGroupResult> = _addGroupResult

    private val _joinGroupResult = MutableLiveData<JoinGroupResponse>()
    val joinGroupResult: LiveData<JoinGroupResponse> = _joinGroupResult

    private val groupInfo = MutableLiveData<Group>()

    private val _updateGroupNameResult = MutableLiveData<JoinGroupResponse>()
    val updateGroupNameResult: LiveData<JoinGroupResponse> = _updateGroupNameResult

    private val _deleteMemberResult = MutableLiveData<Int>()
    val deleteMemberResult: LiveData<Int> = _deleteMemberResult

    fun getGroups() {
        viewModelScope.launch {
            _groups.value = repository.getGroups()
        }
    }

    fun addGroup(img: Uri, name: String) {
        viewModelScope.launch {
            _addGroupResult.value = repository.addGroups(img, name)
        }
    }

    fun joinGroup(groupCode: String) {
        viewModelScope.launch {
            _joinGroupResult.value = repository.joinGroup(groupCode)
        }
    }

    fun updateGroupName(name: String) {
        viewModelScope.launch {
            if (groupInfo.value?.groupName != name) {
                _updateGroupNameResult.value =
                    repository.updateGroupName(groupInfo.value?.groupId ?: 0L, name)
            }
        }
    }

    fun deleteGroupMember() {
        viewModelScope.launch {
            _deleteMemberResult.value =
                repository.deleteMember(groupInfo.value?.groupId ?: 0)
        }
    }

    fun setGroup(group: Group) {
        groupInfo.value = group
    }

    fun getGroup(): Group = groupInfo.value ?: Group()
}