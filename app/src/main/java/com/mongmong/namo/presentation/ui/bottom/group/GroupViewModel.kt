package com.mongmong.namo.presentation.ui.bottom.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun getGroups() {
        viewModelScope.launch {
            _groups.postValue(repository.getGroups())
        }
    }
}