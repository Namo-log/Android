package com.gradu.domain.repositories

import com.gradu.domain.model.Activity
import com.gradu.domain.model.ActivityPayment
import com.gradu.domain.model.BaseResponse

interface ActivityRepository {
    /** 활동*/
    // 모임 기록 활동 리스트 조회
    suspend fun getActivities(scheduleId: Long): List<Activity>

    // 활동 정산 조회
    suspend fun getActivityPayment(activityId: Long): ActivityPayment

    // 활동 추가
    suspend fun addActivity(scheduleId: Long, activity: Activity): BaseResponse

    // 활동 수정
    suspend fun editActivity(activityId: Long, activity: Activity, deleteImages: List<Long>): BaseResponse

    // 활동 태그 수정
    suspend fun editActivityTag(activityId: Long, tag: String): BaseResponse

    // 활동 참가자 수정
    suspend fun editActivityParticipants(
        activityId: Long,
        participantsToAdd: List<Long>,
        participantsToRemove: List<Long>
    ): BaseResponse

    // 활동 정산 수정
    suspend fun editActivityPayment(
        activityId: Long,
        payment: ActivityPayment
    ): BaseResponse

    // 활동 삭제
    suspend fun deleteActivity(activityId: Long): BaseResponse
}