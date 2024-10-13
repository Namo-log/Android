package com.mongmong.namo.domain.usecases

import android.net.Uri
import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.ui.community.diary.MoimDiaryViewModel.Companion.PREFIX
import javax.inject.Inject

class EditMoimDiaryUseCase @Inject constructor(
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
    private val diaryRepository: DiaryRepository,
    private val activityRepository: ActivityRepository
) {
    suspend fun execute(
        diary: DiaryDetail,
        activities: List<Activity>,
        deleteImageIds: MutableMap<Long, MutableList<Long>>
    ): DiaryBaseResponse {
        // 새로운 이미지 S3에 업로드
        val newImageUrls = uploadImageToS3UseCase.execute(
            PREFIX,
            diary.diaryImages?.filter { it.diaryImageId == 0L }
                ?.map { Uri.parse(it.imageUrl) }
                ?: emptyList()
        )

        return diaryRepository.editDiary(
            content = diary.content,
            enjoyRating = diary.enjoyRating,
            images = (
                    diary.diaryImages?.filter { it.diaryImageId != 0L }
                        ?.map { it.imageUrl }
                        ?: emptyList()
                    ) + newImageUrls,
            diaryId = diary.diaryId,
            deleteImageIds = emptyList()
        )
    }
}