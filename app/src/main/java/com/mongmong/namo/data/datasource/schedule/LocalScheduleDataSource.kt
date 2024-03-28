package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.local.dao.ScheduleDao
import com.mongmong.namo.data.local.entity.home.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalScheduleDataSource @Inject constructor(
    private val scheduleDao: ScheduleDao
) {
    suspend fun getDailySchedules(startDate: Long, endDate: Long): List<Schedule> {
        var schedulesResult = listOf<Schedule>()
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.getScheduleDaily(startDate, endDate)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "getDailySchedules Success")
                schedulesResult = it
            }.onFailure {
                Log.d("LocalScheduleDataSource", "getDailySchedules Fail")
            }
        }
        return schedulesResult
    }

    suspend fun addSchedule(schedule: Schedule): Long {
        var localId = 0L
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.insertSchedule(schedule)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "addSchedule Success, scheduleId: $it")
                localId = it // 룸디비 일정 추가 결과
            }.onFailure {
                Log.d("LocalScheduleDataSource", "addSchedule Fail")
            }
        }
        return localId
    }

    suspend fun editSchedule(schedule: Schedule) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.updateSchedule(schedule)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "editSchedule Success")
            }.onFailure {
                Log.d("LocalScheduleDataSource", "editSchedule Fail")
            }
        }
    }

    suspend fun deleteSchedule(localId: Long) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.deleteScheduleById(localId)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "deleteSchedule Success")
            }.onFailure {
                Log.d("LocalScheduleDataSource", "deleteSchedule Fail")
            }
        }
    }

    suspend fun updateScheduleAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        status: String
    ) {
        Log.d("LocalScheduleDataSource updateScheduleAfterUpload", "$localId, $serverId")
        scheduleDao.updateScheduleAfterUpload(
            localId,
            isUpload,
            serverId,
            status
        )
    }
}