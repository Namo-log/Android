package com.gradu.data.utils.mappers

import com.gradu.core.enums.CategoryColor
import com.gradu.data.dto.GetProfileResult
import com.gradu.domain.model.ProfileModel

object ProfileMapper {
    fun GetProfileResult.toModel(): ProfileModel {
        return ProfileModel(
            profileUrl = this.profileImage,
            nickname = this.nickname,
            tag = this.tag.ifEmpty { "1234" },
            name = this.name,
            introduction = this.bio,
            birth = this.birthdate,
            favoriteColor = CategoryColor.findCategoryColorByColorId(this.favoriteColorId),
            isNamePublic = this.nameVisible,
            isBirthPublic = this.birthdayVisible
        )
    }
}