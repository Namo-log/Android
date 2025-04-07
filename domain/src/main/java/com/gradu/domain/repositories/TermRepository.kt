package com.gradu.domain.repositories

import com.gradu.domain.model.TermBody


interface TermRepository {
    suspend fun postTerms(
        termBody: TermBody
    ): Boolean
}