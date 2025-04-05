package com.gradu.domain.repositories

import com.gradu.domain.model.Activity
import com.gradu.domain.model.ActivityPayment
import com.mongmong.namo.domain.model.BaseResponse

interface ActivityRepository {
    /** 활동*/
    // 모임 기록 활동 리스트 조회
    suspend fun getActivities(scheduleId: Long): List<com.gradu.domain.model.Activity>

    // 활동 정산 조회
    suspend fun getActivityPayment(activityId: Long): com.gradu.domain.model.ActivityPayment

    // 활동 추가
    suspend fun addActivity(scheduleId: Long, activity: com.gradu.domain.model.Activity): BaseResponse

    // 활동 수정
    suspend fun editActivity(activityId: Long, activity: com.gradu.domain.model.Activity, deleteImages: List<Long>): BaseResponse

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
        payment: com.gradu.domain.model.ActivityPayment
    ): BaseResponse

    // 활동 삭제
    suspend fun deleteActivity(activityId: Long): BaseResponse
}