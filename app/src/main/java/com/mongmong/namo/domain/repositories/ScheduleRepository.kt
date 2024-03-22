package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Event

interface ScheduleRepository {
    suspend fun getDailySchedules(
        startDate: Long,
        endDate: Long
    ): List<Event>

    suspend fun addSchedule(
        schedule: Event
    )

    suspend fun editSchedule(
        schedule: Event
    )

    suspend fun deleteSchedule(
        localId: Long,
        serverId: Long
    )

    suspend fun uploadScheduleToServer()

    suspend fun postScheduleToServer(scheduleServerId: Long, scheduleId: Long)
}