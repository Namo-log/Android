package com.gradu.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.profile.RemoteProfileDataSource
import com.mongmong.namo.data.dto.PatchProfileRequest
import com.mongmong.namo.data.utils.common.ErrorHandler.handleError
import com.mongmong.namo.data.utils.mappers.ProfileMapper.toModel
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.ProfileModel
import com.mongmong.namo.domain.repositories.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val remoteProfileDataSource: RemoteProfileDataSource
): ProfileRepository {

    override suspend fun getProfile(): ProfileModel {
        return remoteProfileDataSource.getProfile().result.toModel()
    }

    override suspend fun editProfile(
        profileInfo: PatchProfileRequest
    ): BaseResponse {
        var response = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                remoteProfileDataSource.editProfile(
                    profileInfo
                )
            }.onSuccess {
                response = BaseResponse(code = it.code, message = it.message, isSuccess = it.isSuccess)
            }.onFailure { exception ->
                response = exception.handleError()
                Log.d("ActivityDataSource editActivityTag Fail", response.message)
            }
        }

        return response
    }
}