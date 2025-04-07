package com.gradu.domain.repositories

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.PatchProfileRequest
import com.gradu.domain.model.ProfileModel

interface ProfileRepository {
    suspend fun getProfile(): ProfileModel

    suspend fun editProfile(
        profileInfo: PatchProfileRequest
    ): BaseResponse
}