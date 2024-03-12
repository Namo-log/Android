package com.mongmong.namo.data.datasource

import android.content.ContentValues
import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.GetScheduleIdx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RemoteDiaryDataSource(private val apiService: DiaryApiService) {
    suspend fun addDiaryToServer(
        content: String,
        images: List<File>?,
        serverId: Long
    ): DiaryAddResponse {
        var diaryResponse = DiaryAddResponse(result = GetScheduleIdx(-1))
        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody = serverId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addDiary(contentRequestBody, scheduleIdRequestBody, imageToMultipart(images))
            }.onSuccess {
                diaryResponse = it
            }.onFailure {
                Log.e("RemoteDiaryDataSource.addDiaryToServer", "$it")
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
