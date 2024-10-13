package com.mongmong.namo.data.datasource.diary

import android.util.Log
import com.mongmong.namo.data.dto.DiaryResponse
import com.mongmong.namo.data.dto.GetActivitiesResponse
import com.mongmong.namo.data.dto.GetActivitiesResult
import com.mongmong.namo.data.dto.GetActivityPaymentResponse
import com.mongmong.namo.data.dto.GetActivityPaymentResult
import com.mongmong.namo.data.dto.PatchActivityParticipantsRequest
import com.mongmong.namo.data.dto.PatchActivityPaymentRequest
import com.mongmong.namo.data.dto.PatchActivityRequest
import com.mongmong.namo.data.dto.PostActivityRequest
import com.mongmong.namo.data.remote.ActivityApiService
import com.mongmong.namo.data.utils.mappers.ActivityMapper.toDTO
import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.ActivityPayment
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.ParticipantInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityDataSource @Inject constructor(
    val apiService: ActivityApiService
) {
    /** 활동 */
    // 활동 조회
    suspend fun getActivities(scheduleId: Long): List<GetActivitiesResult> {
        var response = GetActivitiesResponse(emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getActivities(scheduleId)
            }.onSuccess {
                Log.d("ActivityDataSource getActivities Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource getActivities Fail", "$it")
            }
        }
        return response.result
    }

    // 활동 정산 조회
    suspend fun getActivityPayment(activityId: Long): GetActivityPaymentResult {
        var response = GetActivityPaymentResponse(GetActivityPaymentResult())
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getActivityPayment(activityId)
            }.onSuccess {
                Log.d("ActivityDataSource getActivityPayment Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource getActivityPayment Fail", "$it")
            }
        }
        return response.result
    }

    // 활동 추가
    suspend fun addActivity(
        scheduleId: Long,
        activity: Activity
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.addActivity(scheduleId,
                    PostActivityRequest(
                        activityStartDate = activity.startDate,
                        activityEndDate = activity.endDate,
                        imageList = activity.images.map { it.imageUrl },
                        location = activity.location.toDTO(),
                        participantIdList = activity.participants.map { it.userId },
                        settlement = activity.payment.toDTO(),
                        tag = activity.tag,
                        title = activity.title
                    )
                )
            }.onSuccess {
                Log.d("ActivityDataSource addActivity Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource addActivity Success", "$it")
            }
        }

        return response
    }

    // 활동 수정
    suspend fun editActivity(
        activityId: Long,
        activity: Activity,
        deleteImages: List<Long>
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editActivity(
                    activityId = activityId,
                    PatchActivityRequest(
                        activityStartDate = activity.startDate,
                        activityEndDate = activity.endDate,
                        imageList = activity.images.map { it.imageUrl },
                        location = activity.location.toDTO(),
                        title = activity.title,
                        deleteImages = deleteImages
                    )
                )
            }.onSuccess {
                Log.d("ActivityDataSource editActivity Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource editActivity Success", "$it")
            }
        }

        return response
    }

    // 활동 태그 수정
    suspend fun editActivityTag(
        activityId: Long,
        tag: String
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editActivityTag(activityId = activityId, tag = tag)
            }.onSuccess {
                Log.d("ActivityDataSource editActivityTag Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource editActivityTag Success", "$it")
            }
        }

        return response
    }

    // 활동 정산 수정
    suspend fun editActivityPayment(
        activityId: Long,
        payment: ActivityPayment
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse()

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editActivityPayment(
                    activityId = activityId,
                    PatchActivityPaymentRequest(
                        amountPerPerson = payment.amountPerPerson,
                        totalAmount = payment.totalAmount,
                        divisionCount = payment.divisionCount,
                        participantIdList = payment.participants.map { it.id }
                    )
                )
            }.onSuccess {
                Log.d("ActivityDataSource editActivityPayment Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource editActivityPayment Success", "$it")
            }
        }
        return response
    }

    // 활동 참가자 수정
    suspend fun editActivityParticipants(
        activityId: Long,
        participantsToAdd: List<Long>,
        participantToRemove: List<Long>
    ): DiaryBaseResponse {
        var response = DiaryBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editActivityParticipants(
                    activityId = activityId,
                    PatchActivityParticipantsRequest(
                        participantsToAdd = participantsToAdd,
                        participantsToRemove = participantToRemove
                    )
                )
            }.onSuccess {
                Log.d("ActivityDataSource editActivityParticipants Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource editActivityParticipants Failure", "$it")
            }
        }

        return response
    }

    // 활동 삭제
    suspend fun deleteActivity(activityId: Long): DiaryBaseResponse {
        var response = DiaryBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteActivity(activityId)
            }.onSuccess {
                Log.d("ActivityDataSource deleteActivity Success", "$it")
                response = it
            }.onFailure {
                Log.d("ActivityDataSource deleteActivity Success", "$it")
            }
        }
        return  response
    }
}