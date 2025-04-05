package com.gradu.domain.model

import com.mongmong.namo.presentation.enums.CategoryColor
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
