package com.mongmong.namo.domain.usecases

import com.mongmong.namo.domain.model.Activity
import com.mongmong.namo.domain.repositories.DiaryRepository
import javax.inject.Inject

class GetActivitiesUseCase @Inject constructor(private val diaryRepository: DiaryRepository) {
    suspend fun execute(scheduleId: Long): List<Activity> {
        val result = emptyList<Activity>()
        diaryRepository.getActivities(scheduleId)
        return  result
    }
}