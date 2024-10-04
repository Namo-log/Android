package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.model.ActivityPayment

interface ActivityRepository {
    /** 활동*/
    // 모임 기록 활동 리스트 조회
    suspend fun getActivities(scheduleId: Long): List<Activity>

    // 활동 정산 조회
    suspend fun getActivityPayment(activityId: Long): ActivityPayment

    // 활동 추가
    suspend fun addActivity(scheduleId: Long, activity: Activity): Boolean

    // 활동 수정
    suspend fun editActivity(activityId: Long, activity: Activity, deleteImages: List<Long>): Boolean

    // 활동 태그 수정
    suspend fun editActivityTag(activityId: Long, tag: String): Boolean

    // 활동 참가자 수정
    suspend fun editActivityParticipants(activityId: Long): Boolean

    // 활동 정산 수정
    suspend fun editActivityPayment(activityId: Long): Boolean

    // 활동 삭제
    suspend fun deleteActivity(activityId: Long): Boolean
}