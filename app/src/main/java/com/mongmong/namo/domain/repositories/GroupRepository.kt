package com.mongmong.namo.domain.repositories

import android.net.Uri
import com.mongmong.namo.domain.model.AddGroupResponse
import com.mongmong.namo.domain.model.Group

interface GroupRepository {
    suspend fun getGroups(): List<Group>

    suspend fun addGroups(img: Uri, name: String): AddGroupResponse
}