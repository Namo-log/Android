package com.mongmong.namo.data.remote.event

import com.mongmong.namo.domain.model.DeleteEventResponse
import com.mongmong.namo.domain.model.EditEventResponse
import com.mongmong.namo.domain.model.GetMonthEventResponse
import com.mongmong.namo.domain.model.PostEventResponse

interface EventView {
    fun onPostEventSuccess(response : PostEventResponse, eventId : Long)
    fun onPostEventFailure(message : String)
    fun onEditEventSuccess(response : EditEventResponse, eventId : Long)
    fun onEditEventFailure(message : String)
}

interface DeleteEventView {
    fun onDeleteEventSuccess(response: DeleteEventResponse, eventId : Long)
    fun onDeleteEventFailure(message : String)
}

interface GetMonthEventView {
    fun onGetMonthEventSuccess(response : GetMonthEventResponse)
    fun onGetMonthEventFailure(message: String)
}

interface GetAllEventView {
    fun onGetAllEventSuccess(response: GetMonthEventResponse)
    fun onGetAllEventFailure(message : String)
}