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
        Log.d("ScheduleRepositoryImpl addSchedule", "$schedule")
        localScheduleDataSource.addSchedule(schedule)
        if (networkChecker.isOnline()) {
            val addResponse = remoteScheduleDataSource.addScheduleToServer(schedule.eventToEventForUpload())
            if (addResponse.code == SUCCESS_CODE) {
                localScheduleDataSource.updateScheduleAfterUpload(
                    localId = schedule.eventId,
                    response = addResponse
                )
            } else {
                Log.d("ScheduleRepositoryImpl addSchedule Fail", "$schedule")
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