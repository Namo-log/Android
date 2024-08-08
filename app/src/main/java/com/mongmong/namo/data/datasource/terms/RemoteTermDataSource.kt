package com.mongmong.namo.data.datasource.terms

import android.util.Log
import com.mongmong.namo.data.remote.TermApiService
import com.mongmong.namo.domain.model.TermBody
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteTermDataSource @Inject constructor(
    private val termApiService: TermApiService
) {
    suspend fun postTerm(
        termBody: TermBody
    ): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                Log.d("RemoteTermDataSource", "aT: ${dsManager.getAccessToken().first()}\nrT: ${dsManager.getRefreshToken().first()}")
                termApiService.postTermsCheck(termBody)
            }.onSuccess {
                Log.d("RemoteTermDataSource", "postTerm Success $it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteTermDataSource", "postTerm Fail $it")
            }
        }
        return isSuccess
    }

}