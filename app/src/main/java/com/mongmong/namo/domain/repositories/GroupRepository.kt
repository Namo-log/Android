package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.Group

interface GroupRepository {
    suspend fun getGroups(): List<Group>
}