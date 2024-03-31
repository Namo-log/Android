package com.mongmong.namo.presentation.ui.bottom.group

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.AddGroupResponse
import com.mongmong.namo.domain.model.Group
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

    private val _addGroupResponse = MutableLiveData<AddGroupResponse>()
    val addGroupResponse: LiveData<AddGroupResponse> = _addGroupResponse

    fun getGroups() {
        viewModelScope.launch {
            _groups.postValue(repository.getGroups())
        }
    }

    fun addGroup(img: Uri, name: String) {
        viewModelScope.launch {
            _addGroupResponse.postValue(repository.addGroups(img, name))
        }
    }
}