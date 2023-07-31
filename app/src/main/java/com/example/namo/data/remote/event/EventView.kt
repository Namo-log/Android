package com.example.namo.data.remote.event

interface EventView {
    fun onPostEventSuccess(response : PostEventResponse, eventId : Long)
    fun onPostEventFailure(message : String, eventId: Long)
    fun onEditEventSuccess(response : EditEventResponse, eventId: Long)
    fun onEditEventFailure(message : String, eventId : Long, serverId : Long)
}

interface DeleteEventView {
    fun onDeleteEventSuccess(response: DeleteEventResponse, eventId : Long)
    fun onDeleteEventFailure(message : String)
}