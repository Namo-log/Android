package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.domain.model.Group
import com.mongmong.namo.domain.repositories.GroupRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val dataSource: GroupDataSource
): GroupRepository {
    override suspend fun getGroups(): List<Group> {
        return dataSource.getGroups()
    }
}