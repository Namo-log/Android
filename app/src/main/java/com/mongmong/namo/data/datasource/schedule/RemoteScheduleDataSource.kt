package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.local.entity.home.EventForUpload
import com.mongmong.namo.data.remote.event.EventRetrofitInterface
import com.mongmong.namo.domain.model.DeleteEventResponse
import com.mongmong.namo.domain.model.EditEventResponse
import com.mongmong.namo.domain.model.EditEventResult
import com.mongmong.namo.domain.model.PostEventResponse
import com.mongmong.namo.domain.model.PostEventResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteScheduleDataSource @Inject constructor(
    private val apiService: EventRetrofitInterface
) {
    suspend fun addScheduleToServer(
        schedule: EventForUpload,
    ): PostEventResponse {
        var scheduleResponse = PostEventResponse(result = PostEventResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.postEvent(schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Failure $it")
            }
        }
        return scheduleResponse
    }

    suspend fun editScheduleToServer(
        scheduleId: Long,
        schedule: EventForUpload
    ) : EditEventResponse {
        var scheduleResponse = EditEventResponse(result = EditEventResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editEvent(scheduleId, schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Fail, $it")
            }
        }
        return scheduleResponse
    }

    suspend fun deleteScheduleToServer(
        scheduleId: Long
    ) : DeleteEventResponse {
        var scheduleResponse = DeleteEventResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteEvent(scheduleId, IS_NOT_GROUP) // 개인
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Fail, $it")
            }
        }
        return scheduleResponse
    }

    companion object {
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
    }
}