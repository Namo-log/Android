package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.dto.EditMoimScheduleProfileRequestBody
import com.mongmong.namo.domain.model.Schedule
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.data.dto.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.data.dto.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.data.utils.mappers.MoimMapper.toDTO
import com.mongmong.namo.data.utils.mappers.MoimMapper.toModel
import com.mongmong.namo.data.utils.mappers.ScheduleMapper.toDTO
import com.mongmong.namo.data.utils.mappers.ScheduleMapper.toModel
import com.mongmong.namo.domain.model.MoimCalendarSchedule
import com.mongmong.namo.domain.model.MoimPreview
import com.mongmong.namo.domain.model.MoimScheduleDetail
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.config.Constants.SUCCESS_CODE
import org.joda.time.DateTime
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val remoteScheduleDataSource: RemoteScheduleDataSource,
    private val networkChecker: NetworkChecker
) : ScheduleRepository {

    /** 개인 */
    override suspend fun getMonthSchedules(startDate: DateTime, endDate: DateTime): List<Schedule> {
        return remoteScheduleDataSource.getMonthSchedules(startDate, endDate).result.map { scheduleData ->
            scheduleData.toModel() // DTO를 도메인 모델로 변환
        }
    }

    override suspend fun addSchedule(schedule: Schedule): Boolean {
        Log.d("ScheduleRepositoryImpl", "addSchedule $schedule")
        return remoteScheduleDataSource.addSchedule(schedule.toDTO()).code == SUCCESS_CODE
    }

    override suspend fun editSchedule(scheduleId: Long, schedule: Schedule): Boolean {
        return remoteScheduleDataSource.editSchedule(
            scheduleId,
            schedule.toDTO()
        ).code == SUCCESS_CODE
    }

    override suspend fun deleteSchedule(scheduleId: Long): Boolean {
        return remoteScheduleDataSource.deleteSchedule(scheduleId).code == SUCCESS_CODE
    }

    // 모임
    override suspend fun editMoimScheduleCategory(category: PatchMoimScheduleCategoryRequestBody): Boolean {
        return remoteScheduleDataSource.editMoimScheduleCategory(category).code == SUCCESS_CODE
    }

    override suspend fun editMoimScheduleAlert(alert: PatchMoimScheduleAlarmRequestBody): Boolean {
        return remoteScheduleDataSource.editMoimScheduleAlert(alert).code == SUCCESS_CODE
    }

    /** 모임 */
    override suspend fun getMoimSchedules(): List<MoimPreview> {
        return remoteScheduleDataSource.getMoimSchedules().result.map { moimData ->
            moimData.toModel() // DTO를 도메인 모델로 변환
        }
    }

    override suspend fun getMoimScheduleDetail(moimScheduleId: Long): MoimScheduleDetail {
        return remoteScheduleDataSource.getMoimSchedueDetail(moimScheduleId).result.toModel()
    }

    override suspend fun getMoimCalendarSchedules(
        moimScheduleId: Long,
        startDate: DateTime,
        endDate: DateTime
    ): List<MoimCalendarSchedule> {
        return remoteScheduleDataSource.getMoimCalendarSchedules(moimScheduleId, startDate, endDate).result.map { scheduleData ->
            scheduleData.toModel()
        }
    }

    override suspend fun addMoimSchedule(moimSchedule: MoimScheduleDetail): Boolean {
        return remoteScheduleDataSource.addMoimSchedule(moimSchedule.toDTO()).code == SUCCESS_CODE
    }

    override suspend fun editMoimSchedule(
        moimSchedule: MoimScheduleDetail,
        participantsToAdd: List<Long>,
        participantsToRemove: List<Long>
    ): Boolean {
        return remoteScheduleDataSource.editMoimSchedule(
            moimSchedule.moimId,
            moimSchedule.toDTO(participantsToAdd, participantsToRemove)
        ).code == SUCCESS_CODE
    }

    override suspend fun deleteMoimSchedule(moimScheduleId: Long): Boolean {
        return remoteScheduleDataSource.deleteMoimSchedule(moimScheduleId).code == SUCCESS_CODE
    }

    override suspend fun editMoimScheduleProfile(
        moimScheduleId: Long,
        title: String,
        imageUrl: String
    ): Boolean {
        return remoteScheduleDataSource.editMoimScheduleProfile(
            moimScheduleId,
            EditMoimScheduleProfileRequestBody(title, imageUrl)
        ).isSuccess
    }

    override suspend fun getGuestInvitaionLink(moimScheduleId: Long): String {
        return remoteScheduleDataSource.getGuestInvitationLink(moimScheduleId).result
    }
}