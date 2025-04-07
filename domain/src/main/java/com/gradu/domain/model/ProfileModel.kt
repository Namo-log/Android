package com.gradu.domain.model

import java.io.Serializable

data class ProfileModel(
    val profileUrl: String?,
    val nickname: String,
    val tag: String,
    val introduction: String,
    val name: String,
    val birth: String,
    val favoriteColor: CategoryColor,
    val isNamePublic: Boolean,
    val isBirthPublic: Boolean
): Serializable

data class PatchProfileRequest(
    val nickname: String = "",
    val birthday: String = "",
    val bio: String = "",
    val profileImage: String? = null,
    val favoriteColorId: Int = 0,
    val nameVisible: Boolean = true,
    val birthdayVisible: Boolean = true
)