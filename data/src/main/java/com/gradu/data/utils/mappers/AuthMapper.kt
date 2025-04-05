package com.gradu.data.utils.mappers

import com.mongmong.namo.data.dto.SignupCompleteRequest
import com.mongmong.namo.domain.model.RegisterInfo

object AuthMapper {
    fun RegisterInfo.toDTO(): SignupCompleteRequest {
        return SignupCompleteRequest(
            bio = this.intro,
            birthday = this.birthday,
            colorId = this.colorId,
            name = this.name,
            nickname = this.nickname,
            profileImage = this.profileImage
        )
    }
}