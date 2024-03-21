package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Event

interface ScheduleRepository {
    suspend fun getSchedule(localId: Long): Event

    suspend fun addSchedule(
        schedule: Event
    )

    suspend fun editSchedule(
        schedule: Event
    )

    suspend fun uploadScheduleToServer()

    suspend fun postScheduleToServer(scheduleServerId: Long, scheduleId: Long)
}