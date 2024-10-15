package com.mongmong.namo.domain.usecases

import android.net.Uri
import android.util.Log
import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiaryImage
import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.ui.community.diary.MoimDiaryViewModel.Companion.PREFIX
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class EditMoimDiaryUseCase @Inject constructor(
    private val uploadImageToS3UseCase: UploadImageToS3UseCase,
    private val diaryRepository: DiaryRepository,
    private val activityRepository: ActivityRepository
) {
    suspend fun execute(
        scheduleId: Long,
        diary: DiaryDetail?,
        activities: List<Activity>,
        deleteDiaryImageIds: List<Long>,
        deleteActivityImageIds: MutableMap<Long, MutableList<Long>>
    ): DiaryBaseResponse {
        Log.d("EditMoimDiaryUseCase", "execute: 시작됨")

        // 결과를 담을 변수를 초기화
        var diaryResponse: DiaryBaseResponse
        val activityResponses = mutableListOf<DiaryBaseResponse>()

        // 활동명 공백 여부 체크
        if(activities.any { activity -> activity.title.isEmpty() }) {
            Log.d("EditMoimDiaryUseCase", "활동명 공백 오류")
            return DiaryBaseResponse(isSuccess = false, message = "활동명을 입력해주세요.")
        }

        if(diary != null) {
            Log.d("EditMoimDiaryUseCase", "다이어리 수정 시작, diaryId: ${diary.diaryId}")

            // 새로운 기록 이미지 S3 업로드
            val newDiaryImageUrls = uploadImageToS3UseCase.execute(
                PREFIX,
                diary.diaryImages?.filter { it.diaryImageId == 0L }
                    ?.map { Uri.parse(it.imageUrl) }
                    ?: emptyList()
            )
            Log.d("EditMoimDiaryUseCase", "새로운 다이어리 이미지 업로드 완료: $newDiaryImageUrls")

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
            Log.d("EditMoimDiaryUseCase", "다이어리 수정 응답: isSuccess = ${diaryResponse.isSuccess}")
        } else diaryResponse = DiaryBaseResponse(result = "", code = 200, message = "", isSuccess = true)

        // 활동 이미지 업로드 및 수정 처리
        coroutineScope {
            activities.forEach { activity ->
                launch {
                    Log.d("EditMoimDiaryUseCase", "활동 수정 시작, activityId: ${activity.activityId}")

                    // 새로운 활동 이미지 S3 업로드
                    val newActivityImageUrls = uploadImageToS3UseCase.execute(
                        PREFIX,
                        activity.images.filter { it.diaryImageId == 0L }
                            .map { Uri.parse(it.imageUrl) }
                    )
                    Log.d("EditMoimDiaryUseCase", "새로운 활동 이미지 업로드 완료: $newActivityImageUrls")

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
                    Log.d("EditMoimDiaryUseCase", "활동 수정 응답: activityId = ${activity.activityId}, isSuccess = ${response.isSuccess}")

                    // 활동 수정 결과 저장
                    activityResponses.add(response)
                }
            }
        }

        // 모든 활동과 기록 수정이 완료된 후, 최종적으로 성공 여부를 확인
        val allActivitiesSuccess = activityResponses.all {
            Log.d("EditMoimDiaryUseCase", "활동 응답: $it")
            it.isSuccess
        }
        Log.d("EditMoimDiaryUseCase", "모든 활동 성공 여부: $allActivitiesSuccess, 다이어리 성공 여부: ${diaryResponse.isSuccess}")

        return if (allActivitiesSuccess && diaryResponse.isSuccess) {
            Log.d("EditMoimDiaryUseCase", "성공적으로 수정 완료")
            diaryResponse
        } else {
            Log.d("EditMoimDiaryUseCase", "수정 실패")
            DiaryBaseResponse(message = "Failed to update all activities or diary")
        }
    }
}
