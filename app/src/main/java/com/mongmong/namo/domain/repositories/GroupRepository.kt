package com.mongmong.namo.domain.repositories

import android.net.Uri
import com.mongmong.namo.domain.model.group.AddGroupResult
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.domain.model.group.JoinGroupResponse
import com.mongmong.namo.domain.model.group.UpdateGroupNameResponse

interface GroupRepository {
    suspend fun getGroups(): List<Group>?
    suspend fun addGroups(img: Uri, name: String): AddGroupResult
    suspend fun joinGroup(groupCode: String): JoinGroupResponse
    suspend fun updateGroupName(groupId: Long, name: String): UpdateGroupNameResponse
    suspend fun deleteMember(groupId: Long): Int
}