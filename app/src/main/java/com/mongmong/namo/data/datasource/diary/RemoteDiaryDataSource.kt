package com.mongmong.namo.data.datasource.diary

import android.content.Context
import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupDiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetScheduleId
import com.mongmong.namo.data.utils.RequestConverter.convertTextRequest
import com.mongmong.namo.data.utils.RequestConverter.imageToMultipart
import com.mongmong.namo.domain.model.group.GetMoimDiaryResponse
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(
    private val diaryApiService: DiaryApiService,
    private val groupDiaryApiService: GroupDiaryApiService,
    private val context: Context
) {
    suspend fun addDiaryToServer(
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
    }

    suspend fun editDiaryToServer(
        diary: Diary,
        images: List<String>?
    ): DiaryResponse {
        var diaryResponse = DiaryResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.editDiary(
                    scheduleId =  diary.scheduleServerId.toString().convertTextRequest(),
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
    }

    suspend fun deleteDiary(scheduleServerId: Long): DiaryResponse {
        var diaryResponse = DiaryResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.deleteDiary(scheduleServerId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteDiary Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteDiary Failure", "$it")
            }
        }
        return diaryResponse
    }

    // 모임 기록 조회
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

    suspend fun deleteMoimMemo(scheduleId: Long): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.deleteMoimMemo(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource patchMoimMemo Success", "$it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteDiaryDataSource patchMoimMemo Failure", "$it")
            }
        }

        return isSuccess
    }

    suspend fun addMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                groupDiaryApiService.addMoimDiary(
                    scheduleId = moimScheduleId,
                    place = place.convertTextRequest(),
                    pay = money.toString().convertTextRequest(),
                    member = (members?.joinToString(",") ?: "").convertTextRequest(),
                    imgs = imageToMultipart(images, context)
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource addMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource addMoimActivity Success", "$it")
            }
        }
    }

    suspend fun editMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    ) {

        withContext(Dispatchers.IO) {
            runCatching {
                groupDiaryApiService.editMoimActivity(
                    moimScheduldId = moimScheduleId,
                    place = place.convertTextRequest(),
                    pay = money.toString().convertTextRequest(),
                    member = (members?.joinToString(",") ?: "").convertTextRequest(),
                    imgs = imageToMultipart(images, context)
                )
            }.onSuccess {
                Log.d("RemoteDiaryDataSource editMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource editMoimActivity Success", "$it")
            }
        }
    }

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

    suspend fun deleteMoimDiary(moimDiaryId: Long): Boolean {
        var isSuccess = false
        withContext(Dispatchers.IO) {
            runCatching {
                groupDiaryApiService.deleteMoimDiary(moimDiaryId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteMoimDiary Success", "$it")
                isSuccess = true
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteMoimDiary Success", "$it")
            }
        }

        return isSuccess
    }
}
