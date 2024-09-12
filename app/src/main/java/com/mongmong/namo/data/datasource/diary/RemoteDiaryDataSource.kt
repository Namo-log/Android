package com.mongmong.namo.data.datasource.diary

import android.content.Context
import android.util.Log
import com.mongmong.namo.data.dto.CategoryInfo
import com.mongmong.namo.data.dto.DiaryRequestImage
import com.mongmong.namo.data.dto.EditDiaryRequest
import com.mongmong.namo.data.dto.GetPersonalDiaryResponse
import com.mongmong.namo.data.dto.GetPersonalDiaryResult
import com.mongmong.namo.data.dto.GetScheduleForDiaryResponse
import com.mongmong.namo.data.dto.GetScheduleForDiaryResult
import com.mongmong.namo.data.dto.Location
import com.mongmong.namo.data.dto.PostDiaryRequest
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupDiaryApiService
import com.mongmong.namo.data.utils.RequestConverter.convertTextRequest
import com.mongmong.namo.data.utils.RequestConverter.imageToMultipart
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.group.GetMoimDiaryResponse
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(
    private val diaryApiService: DiaryApiService,
    private val groupDiaryApiService: GroupDiaryApiService,
    private val context: Context
) {
    /*suspend fun addDiaryToServer(
        diary: Diary,
        images: List<String>?,
    ): DiaryAddResponse {
        var diaryResponse = DiaryAddResponse(result = GetScheduleId(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.addDiary(
                    scheduleId = diary.scheduleServerId.toString().convertTextRequest(),
                    content = (diary.content ?: "").convertTextRequest(),
                    imageToMultipart(images, context)
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource addDiaryToServer Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource addDiaryToServer Failure", "$it")
            }
        }

        return diaryResponse
    }*/

    /*suspend fun editDiaryToServer(
        diary: Diary,
        images: List<String>?
    ): DiaryResponse {
        var diaryResponse = DiaryResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.editDiary(
                    scheduleId = diary.scheduleServerId.toString().convertTextRequest(),
                    content = (diary.content ?: "").convertTextRequest(),
                    imgs = imageToMultipart(images, context)
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource editDiaryToServer Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource editDiaryToServer Failure", "$it")
            }
        }

        return diaryResponse
    }*/

    /** 개인 기록*/
    suspend fun getScheduleForDiary(scheduleId: Long): GetScheduleForDiaryResponse {
        var response = GetScheduleForDiaryResponse(
            result = GetScheduleForDiaryResult(
                scheduleId = 0,
                scheduleTitle = "",
                scheduleStartDate = "",
                locationInfo = Location(kakaoLocationId = "", locationName = ""),
                categoryInfo = CategoryInfo(name = "", colorId = 0),
                hasDiary = true
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getScheduleForDiary(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getScheduleForDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getScheduleForDiary Failure", "$it")
            }
        }
        return response
    }

    /** 개인 기록 조회 */
    suspend fun getPersonalDiary(scheduleId: Long): GetPersonalDiaryResponse {
        var response = GetPersonalDiaryResponse(
            result = GetPersonalDiaryResult(
                content = "", // 빈 문자열
                diaryId = 0L, // 기본값 0L (Long 타입)
                diaryImages = emptyList(), // 빈 리스트
                enjoyRating = 0 // 기본값 0 (Int 타입)
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getPersonalDiary(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getPersonalDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getPersonalDiary Failure", "$it")
            }
        }
        return response
    }

    /** 개인 기록 추가 */
    suspend fun addPersonalDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): DiaryResponse {
        var response = DiaryResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.addPersonalDiary(
                    PostDiaryRequest(
                        content = content,
                        diaryImages = images.map { DiaryRequestImage(it) },
                        enjoyRating = enjoyRating,
                        scheduleId = scheduleId
                    )
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource addPersonalDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource addPersonalDiary Failure", "$it")
            }
        }
        return response
    }
    /** 개인 기록 수정 */
    suspend fun editPersonalDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): DiaryResponse {
        var response = DiaryResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.editPersonalDiary(
                    diaryId,
                    EditDiaryRequest(
                        content = content,
                        enjoyRating = enjoyRating,
                        diaryImages = images.map { DiaryRequestImage(it) },
                        deleteImages = deleteImageIds
                    )
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource editPersonalDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource editPersonalDiary Failure", "$it")
            }
        }
        return response
    }

    /** 개인 기록 삭제 */
    suspend fun deletePersonalDiary(scheduleServerId: Long): DiaryResponse {
        var diaryResponse = DiaryResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.deletePersonalDiary(scheduleServerId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteDiary Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteDiary Failure", "$it")
            }
        }
        return diaryResponse
    }

    /** 모임 기록 조회 */
    suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult {
        var diaryResponse = GetMoimDiaryResponse(
            result = MoimDiaryResult(
                name = "",
                startDate = 0L,
                locationName = "",
                users = emptyList(),
                moimActivities = emptyList()
            )
        )
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getMoimDiary(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getMoimDiary Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getMoimDiary Fail", "$it")
            }
        }

        return diaryResponse.result
    }

    /** 모임 메모 조회 */
    suspend fun getMoimMemo(scheduleId: Long): MoimDiary = withContext(Dispatchers.IO) {
        var result = MoimDiary(
                scheduleId = 0L,
                title = "",
                startDate = 0L,
                _content = "",
                images = emptyList(),
                categoryId = 0L,
                color = 0,
                placeName = ""
            )

        runCatching {
            diaryApiService.getMoimMemo(scheduleId)
        }.onSuccess {
            Log.d("RemoteDiaryDataSource getMoimMemo Success", "$it")
            result = it.result
        }.onFailure {
            Log.d("RemoteDiaryDataSource getMoimMemo Failure", "$it")
        }
        return@withContext result
    }

    /** 모임 메모 수정 */
    suspend fun patchMoimMemo(scheduleId: Long, content: String): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.patchMoimMemo(scheduleId, content)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource patchMoimMemo Success", "$it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteDiaryDataSource patchMoimMemo Failure", "$it")
            }
        }

        return isSuccess
    }

    /** 모임 메모 삭제 */
    suspend fun deleteMoimMemo(scheduleId: Long): Boolean =
        withContext(Dispatchers.IO) {
            var isSuccess = false
            runCatching {
                diaryApiService.deleteMoimMemo(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteMoimMemo Success", "$it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteMoimMemo Failure", "$it")
            }

            Log.d("RemoteDiaryDataSource deleteMoimMemo", "$isSuccess")
            return@withContext isSuccess
        }

    /** 모임 기록 활동 추가 */
    suspend fun addMoimActivity(
        moimScheduleId: Long,
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                val createImagesParts = if (!createImages.isNullOrEmpty()) {
                    imageToMultipart(createImages, context)
                } else {
                    listOf(MultipartBody.Part.createFormData("empty", "", "".convertTextRequest()))
                }
                groupDiaryApiService.addMoimActivity(
                    moimScheduleId = moimScheduleId,
                    activityName = activityName,
                    activityMoney = activityMoney.toString(),
                    participantUserIds = participantUserIds,
                    createImages = createImagesParts
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource addMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource addMoimActivity Success", "$it")
            }
        }
    }

    /** 모임 기록 활동 수정 */
    suspend fun editMoimActivity(
        activityId: Long,
        deleteImageIds: List<Long>?,
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                val createImagesParts = if (!createImages.isNullOrEmpty()) {
                    imageToMultipart(createImages, context)
                } else {
                    listOf(MultipartBody.Part.createFormData("empty", "", "".convertTextRequest()))
                }

                groupDiaryApiService.editMoimActivity(
                    activityId = activityId,
                    deleteImageIds = deleteImageIds,
                    activityName = activityName,
                    activityMoney = activityMoney.toString(),
                    participantUserIds = participantUserIds,
                    createImages = createImagesParts
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource editMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource editMoimActivity Success", "$it")
            }
        }
    }

    /** 모임 기록 활동 삭제 */
    suspend fun deleteMoimActivity(activityId: Long) {
        withContext(Dispatchers.IO) {
            runCatching {
                groupDiaryApiService.deleteMoimActivity(activityId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteMoimActivity Success", "$it")
            }
        }
    }

    /** 모임 기록 삭제 */
    suspend fun deleteMoimDiary(moimScheduleId: Long): Boolean =
        withContext(Dispatchers.IO) {
            var isSuccess = false
            runCatching {
                groupDiaryApiService.deleteMoimDiary(moimScheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteMoimDiary Success", "$it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteMoimDiary Success", "$it")
            }
            return@withContext isSuccess
        }
}
