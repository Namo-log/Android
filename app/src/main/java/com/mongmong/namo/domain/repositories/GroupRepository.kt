package com.mongmong.namo.domain.repositories

import android.net.Uri
import com.mongmong.namo.domain.model.AddGroupResult
import com.mongmong.namo.domain.model.Group
import com.mongmong.namo.domain.model.JoinGroupResponse

interface GroupRepository {
    suspend fun getGroups(): List<Group>
    suspend fun addGroups(img: Uri, name: String): AddGroupResult
    suspend fun joinGroup(groupCode: String): JoinGroupResponse
}