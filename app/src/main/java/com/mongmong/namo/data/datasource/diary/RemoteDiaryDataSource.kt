package com.mongmong.namo.data.datasource.diary

import android.content.Context
import android.util.Log
import com.mongmong.namo.data.dto.CategoryInfo
import com.mongmong.namo.data.dto.DiaryRequestImage
import com.mongmong.namo.data.dto.DiaryResponse
import com.mongmong.namo.data.dto.EditDiaryRequest
import com.mongmong.namo.data.dto.GetActivitiesResponse
import com.mongmong.namo.data.dto.GetActivitiesResult
import com.mongmong.namo.data.dto.GetActivityPaymentResponse
import com.mongmong.namo.data.dto.GetActivityPaymentResult
import com.mongmong.namo.data.dto.GetCalendarDiaryResponse
import com.mongmong.namo.data.dto.GetCalendarDiaryResult
import com.mongmong.namo.data.dto.GetDiaryByDateResponse
import com.mongmong.namo.data.dto.GetDiaryResponse
import com.mongmong.namo.data.dto.GetDiaryResult
import com.mongmong.namo.data.dto.GetScheduleForDiaryResponse
import com.mongmong.namo.data.dto.GetScheduleForDiaryResult
import com.mongmong.namo.data.dto.LocationInfo
import com.mongmong.namo.data.dto.PostDiaryRequest
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupDiaryApiService
import com.mongmong.namo.data.utils.RequestConverter.convertTextRequest
import com.mongmong.namo.data.utils.RequestConverter.imageToMultipart
import com.mongmong.namo.domain.model.DiaryBaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

class RemoteDiaryDataSource @Inject constructor(
    private val diaryApiService: DiaryApiService
) {
    /** 기록 */
    //기록 일정 정보 조회
    suspend fun getScheduleForDiary(scheduleId: Long): GetScheduleForDiaryResponse {
        var response = GetScheduleForDiaryResponse(result = GetScheduleForDiaryResult())
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

    // 기록 상세 조회
    suspend fun getDiary(scheduleId: Long): GetDiaryResponse {
        var response = GetDiaryResponse(result = GetDiaryResult())
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getDiary(scheduleId)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getPersonalDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getPersonalDiary Failure", "$it")
            }
        }
        return response
    }

    // 기록 추가
    suspend fun addPersonalDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.addDiary(
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

    // 기록 수정
    suspend fun editPersonalDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse("")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.editDiary(
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

    // 기록 삭제
    suspend fun deletePersonalDiary(scheduleServerId: Long): DiaryBaseResponse {
        var diaryResponse = DiaryBaseResponse()
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

    // 기록 캘린더 조회
    suspend fun getCalendarDiary(yearMonth: String): GetCalendarDiaryResponse {
        var response = GetCalendarDiaryResponse(GetCalendarDiaryResult())
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getCalendarDiary(yearMonth)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getCalendarDiary Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getCalendarDiary Failure", "$it")
            }
        }
        return response
    }

    // 날짜별 기록 조회 (기록 캘린더)
    suspend fun getDiaryByDate(date: String): GetDiaryByDateResponse {
        var response = GetDiaryByDateResponse(emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                diaryApiService.getDiaryByDate(date)
            }.onSuccess {
                Log.d("RemoteDiaryDataSource getDiaryByDate Success", "$it")
                response = it
            }.onFailure {
                Log.d("RemoteDiaryDataSource getDiaryByDate Fail", "$it")
            }
        }
        return response
    }
}
