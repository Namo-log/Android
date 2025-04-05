package com.gradu.data.utils.mappers

import com.mongmong.namo.data.dto.GetProfileResult
import com.mongmong.namo.domain.model.ProfileModel
import com.mongmong.namo.presentation.enums.CategoryColor

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