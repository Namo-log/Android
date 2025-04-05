package com.gradu.data.dto

import com.mongmong.namo.domain.model.BaseResponse

data class GetProfileResponse(
    val result: GetProfileResult
): BaseResponse()

data class GetProfileResult(
    val nickname: String = "",
    val name: String = "",
    val tag: String = "",
    val birthdate: String = "",
    val bio: String = "",
    val profileImage: String? = null,
    val favoriteColorId: Int = 0,
    val nameVisible: Boolean = true,
    val birthdayVisible: Boolean = true
)

data class PatchProfileRequest(
    val nickname: String = "",
    val birthday: String = "",
    val bio: String = "",
    val profileImage: String? = null,
    val favoriteColorId: Int = 0,
    val nameVisible: Boolean = true,
    val birthdayVisible: Boolean = true
)