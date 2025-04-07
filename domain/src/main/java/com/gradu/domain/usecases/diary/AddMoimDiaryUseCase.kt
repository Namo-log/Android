package com.gradu.domain.usecases.diary

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.DiaryDetail
import com.gradu.domain.repositories.DiaryRepository
import com.gradu.domain.usecases.image.UploadImageToS3UseCase
import com.sun.jndi.toolkit.url.Uri
import javax.inject.Inject

class AddMoimDiaryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val uploadImageToS3UseCase: UploadImageToS3UseCase
) {
    suspend fun execute(
        diary: DiaryDetail,
        scheduleId: Long
    ): BaseResponse {
        val newImageUrls = uploadImageToS3UseCase.execute(PREFIX, (diary.diaryImages).map { Uri.parse(it.imageUrl) })
        return diaryRepository.addDiary(
            content = diary.content,
            enjoyRating = diary.enjoyRating,
            images = newImageUrls,
            scheduleId = scheduleId
        )
    }

    companion object {
        const val PREFIX = "diary"
    }
}