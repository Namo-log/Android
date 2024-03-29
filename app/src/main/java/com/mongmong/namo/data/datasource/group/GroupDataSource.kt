package com.mongmong.namo.data.datasource.group

import android.util.Log
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.domain.model.Group
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupDataSource @Inject constructor(
    private val apiService: GroupApiService
) {
    suspend fun getGroups(): List<Group> {
        var groups = emptyList<Group>()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getGroups()
            }.onSuccess {
                Log.d("GroupDataSource Success", "$it")
                groups = it.result
            }.onFailure {
                Log.d("GroupDataSource Fail", "$it")
            }
        }
        return groups
    }
}