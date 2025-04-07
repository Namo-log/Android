package com.gradu.domain.usecases.profile

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.PatchProfileRequest
import com.gradu.domain.repositories.ProfileRepository
import com.gradu.domain.usecases.image.UploadImageToS3UseCase
import com.sun.jndi.toolkit.url.Uri
import javax.inject.Inject

class EditProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) {
    suspend operator fun invoke(
        profileImage: String,
        nickname: String,
        colorId: Int,
        birthday: String,
        intro: String,
        isBirthdayPublic: Boolean,
        isNamePublic: Boolean
    ): BaseResponse {
        val newImageUrl = uploadImageToS3UseCase.execute(PREFIX, listOf<Uri>(Uri.parse(profileImage)))
        return profileRepository.editProfile(
            PatchProfileRequest(
                nickname = nickname,
                favoriteColorId = colorId,
                birthday = birthday,
                bio = intro,
                profileImage = newImageUrl[0],
                nameVisible = isNamePublic,
                birthdayVisible = isBirthdayPublic
            )
        )
    }

    companion object {
        const val PREFIX = "profile"
    }
}