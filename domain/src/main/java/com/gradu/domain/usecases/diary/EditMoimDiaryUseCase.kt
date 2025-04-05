package com.gradu.domain.usecases.diary

import android.net.Uri
import android.util.Log
import com.gradu.domain.model.Activity
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.gradu.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecases.image.UploadImageToS3UseCase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditMoimDiaryUseCase @Inject constructor(
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
    private val diaryRepository: DiaryRepository,
    private val activityRepository: com.gradu.domain.repositories.ActivityRepository
) {
    suspend fun execute(
        scheduleId: Long,
        diary: DiaryDetail?,
        activities: List<com.gradu.domain.model.Activity>,
        deleteDiaryImageIds: List<Long>,
        deleteActivityImageIds: MutableMap<Long, MutableList<Long>>
    ): BaseResponse {
        // 결과를 담을 변수를 초기화
        var diaryResponse: BaseResponse
        val activityResponses = mutableListOf<BaseResponse>()

        // 활동명 공백 여부 체크
        if(activities.any { activity -> activity.title.isEmpty() }) {
            return BaseResponse(isSuccess = false, message = "활동명을 입력해주세요.")
        }

        if(diary != null) {
            // 새로운 기록 이미지 S3 업로드
            val newDiaryImageUrls = uploadImageToS3UseCase.execute(
                DIARY_PREFIX,
                diary.diaryImages?.filter { it.diaryImageId == 0L }
                    ?.map { Uri.parse(it.imageUrl) }
                    ?: emptyList()
            )

            // 기록 수정 요청
            diaryResponse = diaryRepository.editDiary(
                content = diary.content,
                enjoyRating = diary.enjoyRating,
                images = (
                        diary.diaryImages?.filter { it.diaryImageId != 0L }
                            ?.map { it.imageUrl }
                            ?: emptyList()
                        ) + newDiaryImageUrls,
                diaryId = diary.diaryId,
                deleteImageIds = deleteDiaryImageIds
            )
        } else diaryResponse = BaseResponse(code = 200, message = "", isSuccess = true)

        // 활동 이미지 업로드 및 수정 처리
        coroutineScope {
            activities.forEach { activity ->
                launch {
                    // 새로운 활동 이미지 S3 업로드
                    val newActivityImageUrls = uploadImageToS3UseCase.execute(
                        ACTIVITY_PREFIX,
                        activity.images.filter { it.diaryImageId == 0L }
                            .map { Uri.parse(it.imageUrl) }
                    )

                    val newActivityImages = newActivityImageUrls.map { DiaryImage(0L, it, 1) }

                    // 활동 추가 or 수정
                    val response = if (activity.activityId == 0L) {
                        activityRepository.addActivity(scheduleId = scheduleId, activity = activity)
                    }
                    else {
                        activityRepository.editActivity(
                            activity.activityId,
                            activity.copy(images = activity.images.filter { it.diaryImageId != 0L } + newActivityImages),
                            deleteActivityImageIds[activity.activityId] ?: emptyList()
                        )
                    }

                    // 활동 수정 결과 저장
                    activityResponses.add(response)
                }
            }
        }

        // 모든 활동과 기록 수정이 완료된 후, 최종적으로 성공 여부를 확인
        val allActivitiesSuccess = activityResponses.all { it.isSuccess }

        return if (allActivitiesSuccess && diaryResponse.isSuccess) {
            Log.d("EditMoimDiaryUseCase", "수정 완료")
            diaryResponse
        } else {
            Log.d("EditMoimDiaryUseCase", "수정 실패")
            BaseResponse(message = "Failed to update all activities or diary")
        }
    }

    companion object {
        const val DIARY_PREFIX = "diary"
        const val ACTIVITY_PREFIX = "activity"
    }
}
