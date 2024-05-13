package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.terms.RemoteTermDataSource
import com.mongmong.namo.domain.model.TermBody
import com.mongmong.namo.domain.repositories.TermRepository
import javax.inject.Inject

class TermRepositoryImpl @Inject constructor(
    private val remoteTermDataSource: RemoteTermDataSource
) : TermRepository {
    override suspend fun postTerms(termBody: TermBody): Boolean {
        return remoteTermDataSource.postTerm(termBody)
    }
}