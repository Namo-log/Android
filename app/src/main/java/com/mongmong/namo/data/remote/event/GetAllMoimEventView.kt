package com.mongmong.namo.data.remote.event

import com.mongmong.namo.domain.model.GetMonthEventResponse

interface GetAllMoimEventView {
    fun onGetAllMoimEventSuccess (response : GetMonthEventResponse)
    fun onGetAllMoimEventFailure (message : String)
}

interface GetMonthMoimEventView {
    fun onGetMonthMoimEventSuccess(response : GetMonthEventResponse)
    fun onGetMonthMoimEventFailure(message: String)
}