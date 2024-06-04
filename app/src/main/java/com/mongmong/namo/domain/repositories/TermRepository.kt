package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.TermBody

interface TermRepository {
    suspend fun postTerms(
        termBody: TermBody
    ): Boolean
}