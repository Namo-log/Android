package com.mongmong.namo.data.datasource.group

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.domain.model.group.AddGroupResult
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.model.group.UpdateGroupNameRequestBody
import com.mongmong.namo.data.utils.RequestConverter.convertTextRequest
import com.mongmong.namo.data.utils.RequestConverter.uriToMultipart
import com.mongmong.namo.domain.model.group.JoinGroupResult
import com.mongmong.namo.domain.model.group.UpdateGroupNameResponse
import com.mongmong.namo.presentation.utils.NetworkCheckerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupDataSource @Inject constructor(
    private val apiService: GroupApiService,
    private val context: Context
) {
    suspend fun getGroups(): List<Group>? {
        // 네트워크 연결 없을 때
        if(!NetworkCheckerImpl(context).isOnline()) return null

        var groups = emptyList<Group>()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getGroups()
            }.onSuccess {
                Log.d("GroupDataSource getGroups Success", "$it")
                groups = it.result
            }.onFailure {
                Log.d("GroupDataSource getGroups Fail", "$it")
            }
        }
        return groups
    }

    suspend fun addGroup(img: Uri, name: String): AddGroupResult {
        var result = AddGroupResult(groupId = 0L)
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addGroup(uriToMultipart(img, context, false), name.convertTextRequest())
            }.onSuccess {
                Log.d("GroupDataSource addGroup Success", "$it")
                result = it.result
            }.onFailure {
                Log.d("GroupDataSource addGroup Fail", "$it")
            }
        }
        return result
    }

    suspend fun joinGroup(groupCode: String): JoinGroupResponse {
        var response = JoinGroupResponse(JoinGroupResult(0L, ""))
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.joinGroup(groupCode)
            }.onSuccess {
                Log.d("GroupDataSource joinGroup Success", "$it")
                response = it
            }.onFailure {
                Log.d("GroupDataSource joinGroup Fail", "$it")
            }
        }

        return response
    }

    suspend fun updateGroupName(groupId: Long, name: String): UpdateGroupNameResponse {
        var response = UpdateGroupNameResponse(result = 0L)
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.updateGroupName(UpdateGroupNameRequestBody(groupId, name))
            }.onSuccess {
                Log.d("GroupDataSource updateGroupName Success", "$it")
                response = it
            }.onFailure {
                Log.d("GroupDataSource updateGroupName Fail", "$it")
            }
        }
        return response
    }

    suspend fun deleteMember(groupId: Long): Int {
        var result = 0
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteMember(groupId)
            }.onSuccess {
                Log.d("GroupDataSource updateGroupName Success", "$it")
                result = it.code
            }.onFailure {
                Log.d("GroupDataSource updateGroupName Fail", "$it")
            }
        }
        return result
    }
}