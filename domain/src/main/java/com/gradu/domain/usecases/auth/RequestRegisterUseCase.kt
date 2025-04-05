package com.gradu.domain.usecases.auth


import android.net.Uri
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.RegisterInfo
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.domain.usecases.image.UploadImageToS3UseCase
import javax.inject.Inject

class RequestRegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) {
    suspend operator fun invoke(
        profileImage: Uri,
        name: String,
        nickname: String,
        colorId: Int,
        birthday: String,
        intro: String
    ): BaseResponse {
        val newImageUrl = uploadImageToS3UseCase.execute(PREFIX, listOf<Uri>(profileImage))
        return authRepository.postSignupComplete(
            RegisterInfo(
                name = name,
                nickname = nickname,
                colorId = colorId,
                birthday = birthday,
                intro = intro,
                profileImage = newImageUrl[0]
            )
        )
    }

    companion object {
        const val PREFIX = "profile"
    }
}