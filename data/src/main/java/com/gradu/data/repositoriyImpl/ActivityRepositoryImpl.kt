package com.gradu.data.repositoriyImpl

import com.gradu.data.datasource.diary.RemoteActivityDataSource
import com.gradu.domain.model.Activity
import com.gradu.domain.model.ActivityPayment
import com.gradu.domain.model.BaseResponse
import com.gradu.domain.repositories.ActivityRepository
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityDataSource: RemoteActivityDataSource
): ActivityRepository {
    /** 모임 기록 활동 */
    // 활동 리스트 조회
    override suspend fun getActivities(scheduleId: Long): List<Activity> {
        return activityDataSource.getActivities(scheduleId).map { it.toModel() }
    }

    // 활동 정산 조회
    override suspend fun getActivityPayment(activityId: Long): ActivityPayment {
        return activityDataSource.getActivityPayment(activityId).toModel()
    }

    // 활동 추가
    override suspend fun addActivity(scheduleId: Long, activity: Activity): BaseResponse {
        return activityDataSource.addActivity(scheduleId, activity)
    }

    // 활동 수정
    override suspend fun editActivity(
        activityId: Long,
        activity: Activity,
        deleteImages: List<Long>
    ): BaseResponse {
        return activityDataSource.editActivity(activityId, activity, deleteImages)
    }

    // 활동 태그 수정
    override suspend fun editActivityTag(activityId: Long, tag: String): BaseResponse {
        return activityDataSource.editActivityTag(activityId, tag)
    }

    // 활동 참가자 수정
    override suspend fun editActivityParticipants(
        activityId: Long,
        participantsToAdd: List<Long>,
        participantsToRemove: List<Long>
    ): BaseResponse {
        return activityDataSource.editActivityParticipants(activityId, participantsToAdd, participantsToRemove)
    }

    // 활동 정산 수정
    override suspend fun editActivityPayment(activityId: Long, payment: ActivityPayment): BaseResponse {
        return activityDataSource.editActivityPayment(activityId, payment)
    }

    // 활동 삭제
    override suspend fun deleteActivity(activityId: Long): BaseResponse {
        return activityDataSource.deleteActivity(activityId)
    }
}