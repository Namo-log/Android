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
        Log.d("LocalScheduleDataSource addSchedule", "$schedule")
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleDao.insertEvent(schedule)
            }.onSuccess {
                Log.d("LocalScheduleDataSource addSchedule Success", "$schedule")
            }.onFailure {
                Log.d("LocalScheduleDataSource addSchedule Fail", "$schedule")
            }
        }
    }

    suspend fun updateScheduleAfterUpload(localId: Long, response: PostEventResponse) {
        Log.d("LocalScheduleDataSource updateScheduleAfterUpload", "$localId, $response")
        scheduleDao.updateEventAfterUpload(
            localId,
            LocalDiaryDataSource.UPLOAD_SUCCESS,
            response.result.eventIdx,
            R.string.event_current_default.toString()
        )
    }
}