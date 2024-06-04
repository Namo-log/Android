package com.mongmong.namo.data.remote.schedule

import com.mongmong.namo.domain.model.GetMonthScheduleResponse

interface GetAllMoimScheduleView {
    fun onGetAllMoimScheduleSuccess (response : GetMonthScheduleResponse)
    fun onGetAllMoimScheduleFailure (message : String)
}

interface GetMonthMoimScheduleView {
    fun onGetMonthMoimScheduleSuccess(response : GetMonthScheduleResponse)
    fun onGetMonthMoimScheduleFailure(message: String)
}