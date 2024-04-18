package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody

interface ScheduleRepository {
    /** 개인 */
    suspend fun getMonthSchedules(
        monthStart: Long,
        monthEnd: Long
    ): List<Schedule>

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
        serverId: Long,
        isGroup: Boolean
    )

    suspend fun uploadScheduleToServer()

    suspend fun postScheduleToServer(scheduleServerId: Long, scheduleId: Long)

    // 모임
    suspend fun getMonthMoimSchedule(
        yearMonth: String
    ): List<GetMonthScheduleResult>

    suspend fun editMoimScheduleCategory(category: PatchMoimScheduleCategoryBody)

    suspend fun editMoimScheduleAlert(alert: MoimScheduleAlarmBody)


    /** 그룹 */
    suspend fun getGroupAllSchedules(
        groupId: Long
    ): List<MoimSchedule>

    suspend fun addMoimSchedule(
        moimSchedule: AddMoimSchedule
    )

    suspend fun editMoimSchedule(
        moimSchedule: EditMoimSchedule
    )

    suspend fun deleteMoimSchedule(
        moimScheduleId: Long
    )
}