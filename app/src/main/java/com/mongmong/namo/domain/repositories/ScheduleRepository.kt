package com.mongmong.namo.domain.repositories

import com.mongmong.namo.domain.model.Schedule
import com.mongmong.namo.data.dto.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.data.dto.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.MoimPreview
import com.mongmong.namo.domain.model.MoimScheduleDetail
import org.joda.time.DateTime

interface ScheduleRepository {
    /** 개인 */
    suspend fun getMonthSchedules(
        startDate: DateTime, endDate: DateTime
    ): List<Schedule>

    suspend fun addSchedule(
        schedule: Schedule
    ): Boolean

    suspend fun editSchedule(
        scheduleId: Long,
        schedule: Schedule
    ): Boolean

    suspend fun deleteSchedule(
        scheduleId: Long
    ): Boolean

    // 모임
    suspend fun editMoimScheduleCategory(category: PatchMoimScheduleCategoryRequestBody): Boolean

    suspend fun editMoimScheduleAlert(alert: PatchMoimScheduleAlarmRequestBody): Boolean


    /** 모임 */
    suspend fun getMoimSchedules(): List<MoimPreview>

    suspend fun getMoimScheduleDetail(
        moimScheduleId: Long
    ): MoimScheduleDetail

    suspend fun getMoimCalendarSchedules(
        moimScheduleId: Long,
        startDate: DateTime,
        endDate: DateTime
    ): List<MoimCalendarSchedule>

    suspend fun addMoimSchedule(
        moimSchedule: MoimScheduleDetail
    ): Boolean

    suspend fun editMoimSchedule(
        moimSchedule: MoimScheduleDetail,
        participantsToAdd: List<Long>,
        participantsToRemove: List<Long>
    ): Boolean

    suspend fun deleteMoimSchedule(
        moimScheduleId: Long
    ): Boolean

    suspend fun editMoimScheduleProfile(
        moimScheduleId: Long,
        title: String,
        imageUrl: String
    ): Boolean

    suspend fun getGuestInvitaionLink(
        moimScheduleId: Long
    ): String
}