package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.MoimScheduleBody

interface ScheduleRepository {
    /** 개인 */
    suspend fun getMonthSchedules(
        year: Int, month: Int
    ): List<GetMonthScheduleResult>

    suspend fun getDailySchedules(
        startDate: Long,
        endDate: Long
    ): List<Schedule>

    suspend fun addSchedule(
        schedule: ScheduleRequestBody
    ): Boolean

    suspend fun editSchedule(
        scheduleId: Long,
        schedule: ScheduleRequestBody
    ): Boolean

    suspend fun deleteSchedule(
        scheduleId: Long,
        isGroup: Boolean
    ): Boolean

    // 모임
    suspend fun getMonthMoimSchedule(
        yearMonth: String
    ): List<GetMonthScheduleResult>

    suspend fun editMoimScheduleCategory(category: PatchMoimScheduleCategoryRequestBody): Boolean

    suspend fun editMoimScheduleAlert(alert: PatchMoimScheduleAlarmRequestBody): Boolean


    /** 그룹 */
    suspend fun getGroupAllSchedules(
        groupId: Long
    ): List<MoimScheduleBody>

    suspend fun addMoimSchedule(
        moimSchedule: AddMoimScheduleRequestBody
    )

    suspend fun editMoimSchedule(
        moimSchedule: EditMoimScheduleRequestBody
    )

    suspend fun deleteMoimSchedule(
        moimScheduleId: Long
    )
}