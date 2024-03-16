package com.mongmong.namo.data.datasource.diary

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetScheduleIdx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(private val apiService: DiaryApiService) {
    suspend fun addDiaryToServer(
        diary: Diary,
        images: List<File>?,
    ): DiaryAddResponse {
        var diaryResponse = DiaryAddResponse(result = GetScheduleIdx(-1))
        val contentRequestBody = (diary.content ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody = diary.serverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addDiary(contentRequestBody, scheduleIdRequestBody, imageToMultipart(images))
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
        val scheduleIdRequestBody = diary.serverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editDiary(contentRequestBody, scheduleIdRequestBody, imageToMultipart(images))
            }.onSuccess {
                Log.d("RemoteDiaryDataSource addDiaryToServer Success", "$it")
                diaryResponse = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource addDiaryToServer Failure", "$it")
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
