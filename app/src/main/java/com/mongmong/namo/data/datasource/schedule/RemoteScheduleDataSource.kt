package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.data.local.entity.home.ScheduleForUpload
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import com.mongmong.namo.domain.model.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResult
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.MoimSchedule
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.domain.model.PostScheduleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoteScheduleDataSource @Inject constructor(
    private val personalApiService: ScheduleRetrofitInterface,
    private val groupApiService: GroupApiService,
) {
    /** 개인 */
    suspend fun addScheduleToServer(
        schedule: ScheduleForUpload,
    ): PostScheduleResponse {
        var scheduleResponse = PostScheduleResponse(result = PostScheduleResult(-1))

        withContext(Dispatchers.IO) {
            runCatching {
                personalApiService.postSchedule(schedule)
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
                personalApiService.editSchedule(scheduleId, schedule)
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
                personalApiService.deleteSchedule(scheduleId, IS_NOT_GROUP) // 개인
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
                personalApiService.getMonthMoimSchedule(yearMonth)
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
        category: PatchMoimScheduleCategoryBody
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                personalApiService.patchMoimScheduleCategory(category)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Fail")
            }
        }
    }

    /** 그룹 */
    suspend fun getGroupAllSchedules(
        groupId: Long
    ): List<MoimSchedule> {
        var scheduleResponse = GetMoimScheduleResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                groupApiService.getAllMoimSchedule(groupId)
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
        moimSchedule: AddMoimSchedule
    ) {
        var scheduleResponse = AddMoimScheduleResponse(-1)
        withContext(Dispatchers.IO) {
            runCatching {
                groupApiService.postMoimSchedule(moimSchedule)
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
        moimSchedule: EditMoimSchedule
    ) {
        withContext(Dispatchers.IO) {
            runCatching {
                groupApiService.editMoimSchedule(moimSchedule)
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
                groupApiService.deleteMoimSchedule(moimScheduleId)
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