package com.mongmong.namo.domain.usecases

import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(private val activityRepository: ActivityRepository) {

    suspend fun execute(scheduleId: Long): List<Activity> {
        val activities = activityRepository.getActivities(scheduleId)

        // 각 Activity에 대해 Payment 관련 데이터를 가져오고 대입
        return activities.map { activity ->
            val paymentResult = activityRepository.getActivityPayment(activity.activityId)
            activity.copy(payment = paymentResult)
        }
    }
}
