package com.gradu.domain.usecases.profile

import android.net.Uri
import android.util.Log
import com.mongmong.namo.data.dto.PatchProfileRequest
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.RegisterInfo
import com.mongmong.namo.domain.repositories.ProfileRepository
import com.mongmong.namo.domain.usecases.image.UploadImageToS3UseCase
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
        Log.d("EditProfileUseCase", "birthDay: $birthday")
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