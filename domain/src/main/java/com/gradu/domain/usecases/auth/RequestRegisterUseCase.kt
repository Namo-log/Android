package com.gradu.domain.usecases.auth

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.RegisterInfo
import com.gradu.domain.repositories.AuthRepository
import com.gradu.domain.usecases.image.UploadImageToS3UseCase
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