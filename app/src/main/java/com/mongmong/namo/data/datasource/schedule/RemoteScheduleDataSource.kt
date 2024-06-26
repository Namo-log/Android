package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.remote.group.GroupScheduleApiService
import com.mongmong.namo.data.remote.ScheduleApiService
import com.mongmong.namo.domain.model.group.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResult
import com.mongmong.namo.domain.model.group.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.domain.model.PostScheduleResult
import com.mongmong.namo.domain.model.ScheduleRequestBody
import com.mongmong.namo.domain.model.group.AddMoimScheduleRequestBody
import com.mongmong.namo.domain.model.group.EditMoimScheduleRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteScheduleDataSource @Inject constructor(
    private val scheduleApiService: ScheduleApiService,
    private val groupScheduleApiService: GroupScheduleApiService,
) {
    /** 개인 */
    suspend fun getMonthSchedules(
        yearMonth: String
    ): List<GetMonthScheduleResult> {
        var scheduleResponse = GetMonthScheduleResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.getMonthSchedule(yearMonth)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getMonthSchedules Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getMonthSchedules Success $it")
            }
        }
        return scheduleResponse.result
    }

    suspend fun addScheduleToServer(
        schedule: ScheduleRequestBody,
    ): PostScheduleResponse {
        var scheduleResponse = PostScheduleResponse(result = PostScheduleResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.postSchedule(schedule)
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
        schedule: ScheduleRequestBody
    ) : EditScheduleResponse {
        var scheduleResponse = EditScheduleResponse(result = EditScheduleResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.editSchedule(scheduleId, schedule)
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
        scheduleId: Long,
        isGroup: Boolean
    ) : DeleteScheduleResponse {
        var scheduleResponse = DeleteScheduleResponse("")
        val value = if (isGroup) IS_GROUP else IS_NOT_GROUP

        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.deleteSchedule(scheduleId, value)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteScheduleToServer Fail")
            }
        }
        return scheduleResponse
    }

    // 모임
    suspend fun getMonthMoimSchedule(
        yearMonth: String
    ): List<GetMonthScheduleResult> {
        var scheduleResponse = GetMonthScheduleResponse(
            result = emptyList()
        )
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.getMonthMoimSchedule(yearMonth)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteMoimActivity Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteMoimActivity Fail")
            }
        }
        return scheduleResponse.result
    }

    suspend fun editMoimScheduleCategory(
        category: PatchMoimScheduleCategoryRequestBody
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.patchMoimScheduleCategory(category)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Fail")
            }
        }
    }

    suspend fun editMoimScheduleAlert(
        alert: PatchMoimScheduleAlarmRequestBody
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.patchMoimScheduleAlarm(alert)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleAlert Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleAlert Fail $it")
            }
        }
    }

    /** 그룹 */
    suspend fun getGroupAllSchedules(
        groupId: Long
    ): List<MoimScheduleBody> {
        var scheduleResponse = GetMoimScheduleResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                groupScheduleApiService.getAllMoimSchedule(groupId)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getAllMoimSchedules Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getAllMoimSchedules Fail")
            }
        }
        return scheduleResponse.result
    }

    suspend fun addMoimSchedule(
        moimSchedule: AddMoimScheduleRequestBody
    ) {
        var scheduleResponse = AddMoimScheduleResponse(-1)
        withContext(Dispatchers.IO) {
            runCatching {
                groupScheduleApiService.postMoimSchedule(moimSchedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "addMoimSchedule Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "addMoimSchedule Failure")
            }
        }
//        return scheduleResponse
    }

    suspend fun editMoimSchedule(
        moimSchedule: EditMoimScheduleRequestBody
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                groupScheduleApiService.editMoimSchedule(moimSchedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimSchedule Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimSchedule Failure")
            }
        }
    }

    suspend fun deleteMoimSchedule(
        moimScheduleId: Long
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                groupScheduleApiService.deleteMoimSchedule(moimScheduleId)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "deleteMoimSchedule Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteMoimSchedule Failure")
            }
        }
    }

    companion object {
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
    }
}