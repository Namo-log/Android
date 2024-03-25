package com.mongmong.namo.data.datasource.diary

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetMoimDiaryResponse
import com.mongmong.namo.domain.model.GetScheduleIdx
import com.mongmong.namo.domain.model.MoimDiaryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(private val apiService: DiaryApiService) {
    suspend fun getDiary(scheduleId: Long): MoimDiaryResult {
        var diaryResponse = GetMoimDiaryResponse(result = MoimDiaryResult(
            name = "",
            startDate = 0L,
            locationName = "",
            users = emptyList(),
            locationDtos = emptyList()
        ))
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getMoimDiary(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getDiary Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getDiary Fail", "$it")
            }
        }

        return diaryResponse.result
    }
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

    private fun imageToMultipart(imageFiles: List<File>?): List<MultipartBody.Part>? {
        return imageFiles?.map { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }
    }
}
