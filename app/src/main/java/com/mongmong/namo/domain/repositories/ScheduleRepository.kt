package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Schedule

interface ScheduleRepository {
    suspend fun getDailySchedules(
        startDate: Long,
        endDate: Long
    ): List<Schedule>

    suspend fun addSchedule(
        schedule: Schedule
    )

    suspend fun editSchedule(
        schedule: Schedule
    )

    suspend fun deleteSchedule(
        localId: Long,
        serverId: Long
    )

    suspend fun uploadScheduleToServer()

    suspend fun postScheduleToServer(scheduleServerId: Long, scheduleId: Long)
}