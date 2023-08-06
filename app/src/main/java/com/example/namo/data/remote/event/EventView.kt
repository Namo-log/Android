package com.example.namo.data.remote.event

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