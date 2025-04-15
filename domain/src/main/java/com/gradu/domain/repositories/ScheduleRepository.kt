package com.gradu.domain.repositories


import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.PatchMoimScheduleAlarmRequestBody
import com.gradu.domain.model.PatchMoimScheduleCategoryRequestBody
import com.gradu.domain.model.Schedule
import org.threeten.bp.LocalDateTime

interface ScheduleRepository {
    /** 개인 */
    suspend fun getMonthSchedules(
        startDate: LocalDateTime, endDate: LocalDateTime
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
    suspend fun getMoimSchedules(): List<com.gradu.domain.model.MoimPreview>

    suspend fun getMoimScheduleDetail(
        moimScheduleId: Long
    ): com.gradu.domain.model.MoimScheduleDetail

    suspend fun getMoimCalendarSchedules(
        moimScheduleId: Long,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<com.gradu.domain.model.MoimCalendarSchedule>

    suspend fun addMoimSchedule(
        moimSchedule: com.gradu.domain.model.MoimScheduleDetail
    ): Long

    suspend fun editMoimSchedule(
        moimSchedule: com.gradu.domain.model.MoimScheduleDetail,
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

    suspend fun inviteMoimParticipant(
        moimScheduleId: Long,
        memberIdsToInvite: List<Long>
    ): BaseResponse

    suspend fun getGuestInvitationLink(
        moimScheduleId: Long
    ): String
}