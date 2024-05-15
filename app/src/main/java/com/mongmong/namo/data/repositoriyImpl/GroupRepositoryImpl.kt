package com.mongmong.namo.data.repositoriyImpl

import android.net.Uri
import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.domain.model.group.AddGroupResult
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.repositories.GroupRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val dataSource: GroupDataSource
): GroupRepository {
    override suspend fun getGroups(): List<Group>? {
        return dataSource.getGroups()
    }

    override suspend fun addGroups(img: Uri, name: String): AddGroupResult {
        return dataSource.addGroup(img, name)
    }

    override suspend fun joinGroup(groupCode: String): JoinGroupResponse {
        return dataSource.joinGroup(groupCode)
    }

    override suspend fun updateGroupName(groupId: Long, name: String): JoinGroupResponse {
        return dataSource.updateGroupName(groupId, name)
    }

    override suspend fun deleteMember(groupId: Long): Int {
        return dataSource.deleteMember(groupId)
    }
}