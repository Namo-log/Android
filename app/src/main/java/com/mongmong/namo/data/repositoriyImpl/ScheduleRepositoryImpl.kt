package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.domain.repositories.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val localScheduleDataSource: LocalScheduleDataSource,
    private val remoteScheduleDataSource: RemoteScheduleDataSource,
    private val networkChecker: NetworkChecker
) : ScheduleRepository {

    override suspend fun getSchedule(localId: Long): Event {
        TODO("Not yet implemented")
    }

    override suspend fun addSchedule(schedule: Event) {
        Log.d("ScheduleRepositoryImpl", "addSchedule $schedule")
        localScheduleDataSource.addSchedule(schedule)
        if (networkChecker.isOnline()) {
            val addResponse = remoteScheduleDataSource.addScheduleToServer(schedule.eventToEventForUpload())
            if (addResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "addSchedule Success")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.eventId,
                    serverId = addResponse.result.eventIdx
                )
            } else {
                Log.d("ScheduleRepositoryImpl", "addSchedule Fail, code = ${addResponse.code}, message = ${addResponse.message}")
            }
        }
    }

    override suspend fun editSchedule(schedule: Event) {
        Log.d("ScheduleRepositoryImpl", "editSchedule $schedule")
        localScheduleDataSource.editSchedule(schedule)
        if (networkChecker.isOnline()) {
            val editResponse = remoteScheduleDataSource.editScheduleToServer(schedule.eventId, schedule.eventToEventForUpload())
            if (editResponse.code == SUCCESS_CODE) {
                Log.d("ScheduleRepositoryImpl", "editSchedule Success")
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.eventId,
                    serverId = editResponse.result.eventIdx
                )
            } else {
                Log.d("ScheduleRepositoryImpl", "editSchedule Fail, code = ${editResponse.code}, message = ${editResponse.message}")
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
    }

}