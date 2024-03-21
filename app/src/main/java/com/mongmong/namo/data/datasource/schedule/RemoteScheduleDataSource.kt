package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.local.entity.home.EventForUpload
import com.mongmong.namo.data.remote.event.EventRetrofitInterface
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
                Log.d("RemoteScheduleDataSource addScheduleToServer Success", "$it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource addScheduleToServer Failure", "$it")
            }
        }
        return scheduleResponse
    }
}