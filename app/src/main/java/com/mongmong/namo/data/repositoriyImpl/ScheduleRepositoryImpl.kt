package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.config.Constants.SUCCESS_CODE
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val localScheduleDataSource: LocalScheduleDataSource,
    private val remoteScheduleDataSource: RemoteScheduleDataSource,
    private val networkChecker: NetworkChecker
) : ScheduleRepository {

    /** 개인 */
    override suspend fun getMonthSchedules(yearMonth: String): List<GetMonthScheduleResult> {
        return remoteScheduleDataSource.getMonthSchedules(yearMonth)
    }

    override suspend fun getDailySchedules(startDate: Long, endDate: Long): List<Schedule> {
        return localScheduleDataSource.getDailySchedules(startDate, endDate)
    }

    override suspend fun addSchedule(schedule: ScheduleRequestBody): Boolean {
        Log.d("ScheduleRepositoryImpl", "addSchedule $schedule")
        return remoteScheduleDataSource.addScheduleToServer(schedule).code == SUCCESS_CODE
        /*
        schedule.scheduleId = localScheduleDataSource.addSchedule(schedule) // 로컬에서 일정 생성후 받아온 scheduleId 업데이트
        if (networkChecker.isOnline()) {
            val addResponse =
                remoteScheduleDataSource.addScheduleToServer(schedule.convertLocalScheduleToServer())
            if (addResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "addSchedule Success, $addResponse")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.scheduleId,
                    serverId = addResponse.result.scheduleId,
                    isUpload = UploadState.IS_UPLOAD.state,
                    status = RoomState.DEFAULT.state,
                )
            } else {
                Log.d(
                    "ScheduleRepositoryImpl",
                    "addSchedule Fail, code = ${addResponse.code}, message = ${addResponse.message}"
                )
            }
        }
         */
    }

    override suspend fun editSchedule(scheduleId: Long, schedule: ScheduleRequestBody): Boolean {
        return remoteScheduleDataSource.editScheduleToServer(
            scheduleId,
            schedule
        ).code == SUCCESS_CODE
        /*
        Log.d("ScheduleRepositoryImpl", "editSchedule $schedule")
        localScheduleDataSource.editSchedule(schedule)
        if (networkChecker.isOnline()) {
            val editResponse = remoteScheduleDataSource.editScheduleToServer(
                schedule.serverId,
                schedule.convertLocalScheduleToServer()
            )
            if (editResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "editSchedule Success")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.scheduleId,
                    serverId = editResponse.result.scheduleId,
                    isUpload = UploadState.IS_UPLOAD.state,
                    status = RoomState.DEFAULT.state,
                )
            } else {
                Log.d(
                    "ScheduleRepositoryImpl",
                    "editSchedule Fail, code = ${editResponse.code}, message = ${editResponse.message}"
                )
            }
        }
         */
    }

    override suspend fun deleteSchedule(scheduleId: Long, isGroup: Boolean): Boolean {
        return remoteScheduleDataSource.deleteScheduleToServer(scheduleId, isGroup).code == SUCCESS_CODE
        /*
        // 모임 일정
        if (isGroup) {
            remoteScheduleDataSource.deleteScheduleToServer(serverId, isGroup)
            return
        }

        // 개인 일정
        // room db에 삭제 상태로 변경
        localScheduleDataSource.updateScheduleAfterUpload(
            localId = localId,
            serverId = serverId,
            isUpload = UploadState.IS_NOT_UPLOAD.state,
            status = RoomState.DELETED.state,
        )
        if (networkChecker.isOnline()) {
            // 서버 db에서 삭제
            val deleteResponse = remoteScheduleDataSource.deleteScheduleToServer(serverId, isGroup)
            if (deleteResponse.code == DiaryRepositoryImpl.SUCCESS_CODE) {
                // room db에서 삭제
                localScheduleDataSource.deleteSchedule(localId)
            } else {
                Log.d(
                    "ScheduleRepositoryImpl",
                    "deleteSchedule Fail, code = ${deleteResponse.code}, message = ${deleteResponse.message}"
                )
            }
        }
         */
    }

    // 모임
    override suspend fun getMonthMoimSchedule(yearMonth: String): List<GetMonthScheduleResult> {
        return remoteScheduleDataSource.getMonthMoimSchedule(yearMonth)
    }

    override suspend fun editMoimScheduleCategory(category: PatchMoimScheduleCategoryRequestBody): Boolean {
        return remoteScheduleDataSource.editMoimScheduleCategory(category).code == SUCCESS_CODE
    }

    override suspend fun editMoimScheduleAlert(alert: PatchMoimScheduleAlarmRequestBody): Boolean {
        return remoteScheduleDataSource.editMoimScheduleAlert(alert).code == SUCCESS_CODE
    }

    /** 그룹 */
    override suspend fun getGroupAllSchedules(groupId: Long): List<MoimScheduleBody> {
        return remoteScheduleDataSource.getGroupAllSchedules(groupId)
    }

    override suspend fun addMoimSchedule(moimSchedule: AddMoimScheduleRequestBody) {
        return remoteScheduleDataSource.addMoimSchedule(moimSchedule)
    }

    override suspend fun editMoimSchedule(moimSchedule: EditMoimScheduleRequestBody) {
        return remoteScheduleDataSource.editMoimSchedule(moimSchedule)
    }

    override suspend fun deleteMoimSchedule(moimScheduleId: Long) {
        return remoteScheduleDataSource.deleteMoimSchedule(moimScheduleId)
    }

}