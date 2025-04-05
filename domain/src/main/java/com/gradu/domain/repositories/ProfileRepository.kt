package com.gradu.domain.repositories

import com.mongmong.namo.data.dto.PatchProfileRequest
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.ProfileModel

interface ProfileRepository {
    suspend fun getProfile(): ProfileModel

    suspend fun editProfile(
        profileInfo: PatchProfileRequest
    ): BaseResponse
}