package com.gradu.data.datasource.profile

import android.util.Log
import com.mongmong.namo.data.dto.GetProfileResponse
import com.mongmong.namo.data.dto.GetProfileResult
import com.mongmong.namo.data.dto.PatchProfileRequest
import com.mongmong.namo.data.remote.ProfileApiService
import com.mongmong.namo.data.utils.common.ErrorHandler.handleError
import com.mongmong.namo.domain.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteProfileDataSource @Inject constructor(
    private val apiService: ProfileApiService
) {
    suspend fun getProfile(): GetProfileResponse {
        var response = GetProfileResponse(result = GetProfileResult())
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getProfile()
            }.onSuccess {
                Log.d("RemoteProfileDataSource getProfile Success", "$it")
                response = it
            }.onFailure { exception ->
                Log.d("RemoteProfileDataSource getProfile Failure", "${exception.handleError()}")
            }
        }
        return response
    }

    suspend fun editProfile(
        patchProfileRequest: PatchProfileRequest
    ): BaseResponse {
        var response = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.patchProfileInfo(patchProfileRequest)
            }.onSuccess {
                response = BaseResponse(code = it.code, message = it.message, isSuccess = it.isSuccess)
            }.onFailure { exception ->
                response = exception.handleError()
                Log.d("RemoteProfileDataSource editProfile Fail", response.message)
            }
        }

        return response
    }
}