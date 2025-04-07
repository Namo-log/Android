package com.gradu.domain.usecases.activity

import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(private val activityRepository: com.gradu.domain.repositories.ActivityRepository) {

    suspend fun execute(scheduleId: Long): List<com.gradu.domain.model.Activity> {
        val activities = activityRepository.getActivities(scheduleId)

        // 각 Activity에 대해 Payment 관련 데이터를 가져오고 대입
        return activities.map { activity ->
            val paymentResult = activityRepository.getActivityPayment(activity.activityId)
            activity.copy(payment = paymentResult)
        }
    }
}
