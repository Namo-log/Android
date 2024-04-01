package com.mongmong.namo.data.datasource.group

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.domain.model.AddGroupResponse
import com.mongmong.namo.domain.model.AddGroupResult
import com.mongmong.namo.domain.model.Group
import com.mongmong.namo.domain.model.JoinGroupResponse
import com.mongmong.namo.presentation.utils.RequestConverter.convertTextRequest
import com.mongmong.namo.presentation.utils.RequestConverter.uriToMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupDataSource @Inject constructor(
    private val apiService: GroupApiService,
    private val context: Context
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

    suspend fun addGroup(img: Uri, name: String): AddGroupResult {
        var result = AddGroupResult(moimId = 0L)
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addGroup(uriToMultipart(img, context), name.convertTextRequest())
            }.onSuccess {
                Log.d("GroupDataSource Success", "$it")
                result = it.result
            }.onFailure {
                Log.d("GroupDataSource Fail", "$it")
            }
        }
        return result
    }

    suspend fun joinGroup(groupCode: String): JoinGroupResponse {
        var response = JoinGroupResponse(result = 0L)
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.joinGroup(groupCode)
            }.onSuccess {
                Log.d("GroupDataSource Success", "$it")
                response = it
            }.onFailure {
                Log.d("GroupDataSource Fail", "$it")
            }
        }

        return response
    }
}