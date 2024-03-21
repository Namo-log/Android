package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.R
import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.local.dao.EventDao
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.model.PostEventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalScheduleDataSource @Inject constructor(
    private val scheduleDao: EventDao
) {
    suspend fun addSchedule(schedule: Event) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.insertEvent(schedule)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "addSchedule Success")
            }.onFailure {
                Log.d("LocalScheduleDataSource", "addSchedule Fail")
            }
        }
    }

    suspend fun editSchedule(schedule: Event) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.updateEvent(schedule)
            }.onSuccess {
                Log.d("LocalScheduleDataSource", "editSchedule Success")
            }.onFailure {
                Log.d("LocalScheduleDataSource", "editSchedule Fail")
            }
        }
    }

    suspend fun updateScheduleAfterUpload(localId: Long, serverId: Long) {
        Log.d("LocalScheduleDataSource updateScheduleAfterUpload", "$localId, $serverId")
        scheduleDao.updateEventAfterUpload(
            localId,
            LocalDiaryDataSource.UPLOAD_SUCCESS,
            serverId,
            R.string.event_current_default.toString()
        )
    }
}