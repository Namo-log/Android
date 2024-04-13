package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.local.entity.home.ScheduleForUpload
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResult
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.domain.model.PostScheduleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteScheduleDataSource @Inject constructor(
    private val apiService: ScheduleRetrofitInterface
) {
    /** 개인 */
    suspend fun addScheduleToServer(
        schedule: ScheduleForUpload,
    ): PostScheduleResponse {
        var scheduleResponse = PostScheduleResponse(result = PostScheduleResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.postSchedule(schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Failure")
            }
        }
        return scheduleResponse
    }

    suspend fun editScheduleToServer(
        scheduleId: Long,
        schedule: ScheduleForUpload
    ) : EditScheduleResponse {
        var scheduleResponse = EditScheduleResponse(result = EditScheduleResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.editSchedule(scheduleId, schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Fail")
            }
        }
        return scheduleResponse
    }

    suspend fun deleteScheduleToServer(
        scheduleId: Long
    ) : DeleteScheduleResponse {
        var scheduleResponse = DeleteScheduleResponse("")

        withContext(Dispatchers.IO) {
            runCatching {
                apiService.deleteSchedule(scheduleId, IS_NOT_GROUP) // 개인
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Fail")
            }
        }
        return scheduleResponse
    }

    /** 모임 */
    suspend fun getMonthMoimSchedule(
        yearMonth: String
    ): List<GetMonthScheduleResult> {
        var scheduleResponse = GetMonthScheduleResponse(
            result = emptyList()
        )
        withContext(Dispatchers.IO) {
            runCatching {
                apiService.getMonthMoimSchedule(yearMonth)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteMoimActivity Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteMoimActivity Fail")
            }
        }
        return scheduleResponse.result
    }

    companion object {
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
    }
}