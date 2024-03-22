package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.R
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.domain.repositories.ScheduleRepository
import com.mongmong.namo.presentation.config.RoomState
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val localScheduleDataSource: LocalScheduleDataSource,
    private val remoteScheduleDataSource: RemoteScheduleDataSource,
    private val networkChecker: NetworkChecker
) : ScheduleRepository {

    override suspend fun getDailySchedules(startDate: Long, endDate: Long): List<Schedule> {
        val list = localScheduleDataSource.getDailySchedules(startDate, endDate)
        Log.d("ScheduleRepositoryImpl", "getDailySchedules")
        list.forEach { event ->
            Log.d("getDailySchedules", "${event.scheduleId}, ${event.title}")
        }
        return list
    }

    override suspend fun addSchedule(schedule: Schedule) {
        Log.d("ScheduleRepositoryImpl", "addSchedule $schedule")
        localScheduleDataSource.addSchedule(schedule)
        if (networkChecker.isOnline()) {
            val addResponse =
                remoteScheduleDataSource.addScheduleToServer(schedule.eventToScheduleForUpload())
            if (addResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "addSchedule Success")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.scheduleId,
                    serverId = addResponse.result.scheduleIdx,
                    isUpload = IS_UPLOAD,
                    status = RoomState.DEFAULT.state,
                )
            } else {
                Log.d(
                    "ScheduleRepositoryImpl",
                    "addSchedule Fail, code = ${addResponse.code}, message = ${addResponse.message}"
                )
            }
        }
    }

    override suspend fun editSchedule(schedule: Schedule) {
        Log.d("ScheduleRepositoryImpl", "editSchedule $schedule")
        localScheduleDataSource.editSchedule(schedule)
        if (networkChecker.isOnline()) {
            val editResponse = remoteScheduleDataSource.editScheduleToServer(
                schedule.scheduleId,
                schedule.eventToScheduleForUpload()
            )
            if (editResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "editSchedule Success")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.scheduleId,
                    serverId = editResponse.result.scheduleIdx,
                    isUpload = IS_UPLOAD,
                    status = RoomState.DEFAULT.state,
                )
            } else {
                Log.d(
                    "ScheduleRepositoryImpl",
                    "editSchedule Fail, code = ${editResponse.code}, message = ${editResponse.message}"
                )
            }
        }
    }

    override suspend fun deleteSchedule(localId: Long, serverId: Long) {
        // room db에 삭제 상태로 변경
        localScheduleDataSource.updateScheduleAfterUpload(
            localId = localId,
            serverId = serverId,
            isUpload = IS_NOT_UPLOAD,
            status = RoomState.DELETED.state,
        )
        if (networkChecker.isOnline()) {
            // 서버 db에서 삭제
            val deleteResponse = remoteScheduleDataSource.deleteScheduleToServer(serverId)
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
    }

    override suspend fun uploadScheduleToServer() {
        TODO("Not yet implemented")
    }

    override suspend fun postScheduleToServer(scheduleServerId: Long, scheduleId: Long) {
        TODO("Not yet implemented")
    }

    companion object {
        const val SUCCESS_CODE = 200
        const val IS_UPLOAD = true
        const val IS_NOT_UPLOAD = false
    }

}