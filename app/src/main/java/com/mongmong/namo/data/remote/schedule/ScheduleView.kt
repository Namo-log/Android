package com.mongmong.namo.data.remote.schedule

import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.PostScheduleResponse

interface ScheduleView {
    fun onPostScheduleSuccess(response : PostScheduleResponse, scheduleId : Long)
    fun onPostScheduleFailure(message : String)
    fun onEditScheduleSuccess(response : EditScheduleResponse, scheduleId : Long)
    fun onEditScheduleFailure(message : String)
}

interface DeleteScheduleView {
    fun onDeleteScheduleSuccess(response: DeleteScheduleResponse, scheduleId : Long)
    fun onDeleteScheduleFailure(message : String)
}

interface GetMonthScheduleView {
    fun onGetMonthScheduleSuccess(response : GetMonthScheduleResponse)
    fun onGetMonthScheduleFailure(message: String)
}

interface GetAllScheduleView {
    fun onGetAllScheduleSuccess(response: GetMonthScheduleResponse)
    fun onGetAllScheduleFailure(message : String)
}