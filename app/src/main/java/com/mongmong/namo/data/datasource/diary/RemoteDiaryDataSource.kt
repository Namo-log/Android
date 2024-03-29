package com.mongmong.namo.data.datasource.diary

import android.content.Context
import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetMoimDiaryResponse
import com.mongmong.namo.domain.model.GetScheduleIdx
import com.mongmong.namo.domain.model.MoimDiaryResult
import com.mongmong.namo.presentation.utils.ImageConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(
    private val apiService: DiaryApiService,
    private val context: Context
) {
    suspend fun addDiaryToServer(
        diary: Diary,
        images: List<File>?,
    ): DiaryAddResponse {
        var diaryResponse = DiaryAddResponse(result = GetScheduleIdx(-1))
        val contentRequestBody = (diary.content ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody = diary.scheduleServerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                val image = imageToMultipart(images)
                apiService.addDiary(scheduleIdRequestBody,contentRequestBody, image)
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
        images: List<File>?
    ):  DiaryResponse {
        var diaryResponse = DiaryResponse("")
        val contentRequestBody = (diary.content ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody = diary.scheduleServerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editDiary(scheduleIdRequestBody, contentRequestBody, imageToMultipart(images))
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
                apiService.deleteDiary(scheduleServerId)
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
        var diaryResponse = GetMoimDiaryResponse(result = MoimDiaryResult(
            name = "",
            startDate = 0L,
            locationName = "",
            users = emptyList(),
            moimActivities = emptyList()
        ))
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getMoimDiary(scheduleId)
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
                apiService.patchMoimMemo(scheduleId, content)
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
        val placeRequestBody = place.toRequestBody("text/plain".toMediaTypeOrNull())
        val moneyRequestBody = money.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val member = members?.joinToString(",") ?: ""
        val membersRequestBody = member.toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addMoimDiary(
                    moimScheduleId,
                    placeRequestBody,
                    moneyRequestBody,
                    membersRequestBody,
                    imageToMultipart(ImageConverter.imageToFile(images, context))
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
        val placeRequestBody = place.toRequestBody("text/plain".toMediaTypeOrNull())
        val moneyRequestBody = money.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val member = members?.joinToString(",") ?: ""
        val membersRequestBody = member.toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editMoimActivity(
                    moimScheduleId,
                    placeRequestBody,
                    moneyRequestBody,
                    membersRequestBody,
                    imageToMultipart(ImageConverter.imageToFile(images, context))
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
                apiService.deleteMoimActivity(activityId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource deleteMoimActivity Success", "$it")
            }.onFailure {
                Log.d("RemoteDiaryDataSource deleteMoimActivity Success", "$it")
            }
        }
    }

    private fun imageToMultipart(imageFiles: List<File>?): List<MultipartBody.Part>? {
        return imageFiles?.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }
    }
}
