package com.mongmong.namo.data.datasource.schedule

import android.util.Log
import com.mongmong.namo.data.remote.group.GroupScheduleApiService
import com.mongmong.namo.data.remote.ScheduleApiService
import com.mongmong.namo.data.dto.DeleteScheduleResponse
import com.mongmong.namo.data.dto.EditMoimScheduleProfileRequestBody
import com.mongmong.namo.data.dto.EditMoimScheduleRequestBody
import com.mongmong.namo.data.dto.GetMonthScheduleResponse
import com.mongmong.namo.data.dto.PatchMoimScheduleAlarmRequestBody
import com.mongmong.namo.data.dto.PatchMoimScheduleCategoryRequestBody
import com.mongmong.namo.data.dto.PostScheduleResponse
import com.mongmong.namo.data.dto.EditScheduleResponse
import com.mongmong.namo.data.dto.GetMoimCalendarResponse
import com.mongmong.namo.data.dto.GetMoimDetailResponse
import com.mongmong.namo.data.dto.GetMoimDetailResult
import com.mongmong.namo.data.dto.GetMoimResponse
import com.mongmong.namo.data.dto.MoimBaseResponse
import com.mongmong.namo.data.dto.MoimScheduleRequestBody
import com.mongmong.namo.data.dto.PostMoimScheduleResponse
import com.mongmong.namo.data.dto.ScheduleRequestBody
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.presentation.utils.ScheduleDateConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject

class RemoteScheduleDataSource @Inject constructor(
    private val scheduleApiService: ScheduleApiService,
    private val moimScheduleApiService: GroupScheduleApiService,
) {
    /** 개인 */
    // 일정 조회
    suspend fun getMonthSchedules(
        startDate: DateTime,
        endDate: DateTime
    ): GetMonthScheduleResponse {
        var scheduleResponse = GetMonthScheduleResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.getMonthSchedule(
                    startDate = ScheduleDateConverter.parseDateTimeToServerData(startDate),
                    endDate = ScheduleDateConverter.parseDateTimeToServerData(endDate)
                )
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getMonthSchedules Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getMonthSchedules Success $it")
            }
        }
        return scheduleResponse
    }

    // 일정 생성
    suspend fun addSchedule(
        schedule: ScheduleRequestBody,
    ): PostScheduleResponse {
        var scheduleResponse = PostScheduleResponse(-1)

        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.postSchedule(schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "addScheduleToServer Fail $it")
            }
        }
        return scheduleResponse
    }

    // 일정 수정
    suspend fun editSchedule(
        scheduleId: Long,
        schedule: ScheduleRequestBody
    ) : EditScheduleResponse {
        var scheduleResponse = EditScheduleResponse()

        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.editSchedule(scheduleId, schedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Success, $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editScheduleToServer Fail $it")
            }
        }
        return scheduleResponse
    }

    // 일정 삭제
    suspend fun deleteSchedule(
        scheduleId: Long
    ) : DeleteScheduleResponse {
        var scheduleResponse = DeleteScheduleResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.deleteSchedule(scheduleId)
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
    suspend fun editMoimScheduleCategory(
        category: PatchMoimScheduleCategoryRequestBody
    ): BaseResponse {
        var scheduleResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.patchMoimScheduleCategory(category)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleCategory Fail")
            }
        }
        return scheduleResponse
    }

    suspend fun editMoimScheduleAlert(
        alert: PatchMoimScheduleAlarmRequestBody
    ): BaseResponse {
        var scheduleResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                scheduleApiService.patchMoimScheduleAlarm(alert)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleAlert Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleAlert Fail $it")
            }
        }
        return scheduleResponse
    }

    /** 모임 */
    // 모임 일정 목록 조회
    suspend fun getMoimSchedules(): GetMoimResponse {
        var moimResponse = GetMoimResponse(result = emptyList())
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.getMoimCalendarSchedule()
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getMoimSchedules Success $it")
                moimResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getMoimSchedules Fail $it")
            }
        }
        return moimResponse
    }

    // 모임 일정 상세 조회
    suspend fun getMoimSchedueDetail(
        moimScheduleId: Long
    ): GetMoimDetailResponse {
        var moimDetailResponse = GetMoimDetailResponse(result = GetMoimDetailResult())
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.getMoimScheduleDetail(moimScheduleId)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getMoimSchedueDetail Success $it")
                moimDetailResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getMoimSchedueDetail Fail $it")
            }
        }
        return moimDetailResponse
    }

    // 모임 캘린더 조회
    suspend fun getMoimCalendarSchedules(
        moimId: Long,
        startDate: DateTime,
        endDate: DateTime
    ): GetMoimCalendarResponse {
        var scheduleResponse = GetMoimCalendarResponse(result = arrayListOf())
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.getMoimCalendarSchedule(
                    moimId,
                    ScheduleDateConverter.parseDateTimeToServerData(startDate),
                    ScheduleDateConverter.parseDateTimeToServerData(endDate)
                )
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "getMoimCalendarSchedules Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getMoimCalendarSchedules Fail $it")
            }
        }
        return scheduleResponse
    }

    // 모임 일정 생성
    suspend fun addMoimSchedule(
        moimSchedule: MoimScheduleRequestBody
    ): PostMoimScheduleResponse {
        var scheduleResponse = PostMoimScheduleResponse(-1)
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.postMoimSchedule(moimSchedule)
            }.onSuccess {
                Log.d("RemoteScheduleDataSource", "addMoimSchedule Success $it")
                scheduleResponse = it
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "addMoimSchedule Failure $it")
            }
        }
        return scheduleResponse
    }

    // 모임 일정 수정
    suspend fun editMoimSchedule(
        moimId: Long,
        moimSchedule: EditMoimScheduleRequestBody
    ): BaseResponse {
        var scheduleResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.editMoimSchedule(moimId, moimSchedule)
            }.onSuccess {
                scheduleResponse = it
                Log.d("RemoteScheduleDataSource", "editMoimSchedule Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimSchedule Failure $it")
            }
        }
        return scheduleResponse
    }

    // 모임 일정 삭제
    suspend fun deleteMoimSchedule(
        moimScheduleId: Long
    ): BaseResponse {
        var scheduleResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.deleteMoimSchedule(moimScheduleId)
            }.onSuccess {
                scheduleResponse = it
                Log.d("RemoteScheduleDataSource", "deleteMoimSchedule Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "deleteMoimSchedule Failure")
            }
        }
        return scheduleResponse
    }

    // 모임 일정 프로필 변경
    suspend fun editMoimScheduleProfile(
        moimScheduleId: Long,
        request: EditMoimScheduleProfileRequestBody
    ): BaseResponse {
        var scheduleResponse = BaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.editMoimScheduleProfile(moimScheduleId, request)
            }.onSuccess {
                scheduleResponse = it
                Log.d("RemoteScheduleDataSource", "editMoimScheduleProfile Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "editMoimScheduleProfile Failure $it")
            }
        }
        return scheduleResponse
    }

    // 게스트 초대용 링크 조회
    suspend fun getGuestInvitationLink(
        moimScheduleId: Long
    ): MoimBaseResponse {
        var scheduleResponse = MoimBaseResponse()
        withContext(Dispatchers.IO) {
            runCatching {
                moimScheduleApiService.getGuestInvitationLink(moimScheduleId)
            }.onSuccess {
                scheduleResponse = it
                Log.d("RemoteScheduleDataSource", "getGuestInvitationLink Success $it")
            }.onFailure {
                Log.d("RemoteScheduleDataSource", "getGuestInvitationLink Failure $it")
            }
        }
        return scheduleResponse
    }
}