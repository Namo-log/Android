package com.example.namo.data.remote.event

import android.util.Log
import com.example.namo.config.ApplicationClass
import com.example.namo.data.entity.home.EventForUpload
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventService() {
    private lateinit var eventView : EventView
    private lateinit var deleteEventView : DeleteEventView

    fun setEventView(eventView : EventView) {
        this.eventView = eventView
    }

    fun setDeleteEventView(deleteEventView: DeleteEventView) {
        this.deleteEventView = deleteEventView
    }

    val eventRetrofitInterface = ApplicationClass.sRetrofit.create(EventRetrofitInterface::class.java)

    fun postEvent(body : EventForUpload) {
        eventRetrofitInterface.postEvent(body).enqueue(object : Callback<PostEventResponse> {
            override fun onResponse(call: Call<PostEventResponse>, response: Response<PostEventResponse>) {
                when (response.code()) {
                    200 -> eventView.onPostEventSuccess(response.body() as PostEventResponse, body.eventId)
                    else -> {
                        Log.d("PostEvent", "Success but error")
                        eventView.onPostEventFailure("통신 중 200 외 기타 코드", body.eventId)
                    }
                }
            }

            override fun onFailure(call: Call<PostEventResponse>, t: Throwable) {
                Log.d("PostEvent", "onFailure")
                eventView.onPostEventFailure(t.message ?: "통신 오류", body.eventId)
            }
        })
    }

    fun editEvent(serverIdx : Long, body: EventForUpload) {
        eventRetrofitInterface.editEvent(serverIdx, body).enqueue(object : Callback<EditEventResponse> {
            override fun onResponse(call: Call<EditEventResponse>, response: Response<EditEventResponse>) {
                when (response.code()) {
                    200 -> eventView.onEditEventSuccess(response.body() as EditEventResponse, body.eventId)
                    else -> {
                        Log.d("EditEvent", "Success but error")
                        eventView.onEditEventFailure("통신 중 200 외 기타 코드", body.eventId, serverIdx)
                    }
                }

            }

            override fun onFailure(call: Call<EditEventResponse>, t: Throwable) {
                Log.d("EditEvent", "OnFailure")
                eventView.onEditEventFailure(t.message ?: "통신 오류", body.eventId, serverIdx)
            }
        })
    }

    fun deleteEvent(serverIdx: Long, eventId : Long) {
        eventRetrofitInterface.deleteEvent(serverIdx).enqueue(object : Callback<DeleteEventResponse> {
            override fun onResponse(call: Call<DeleteEventResponse>, response: Response<DeleteEventResponse>) {
                when (response.code()) {
                    200 -> deleteEventView.onDeleteEventSuccess(response.body() as DeleteEventResponse, eventId)
                    else -> {
                        Log.d("DeleteEvent", "Success but error")
                        deleteEventView.onDeleteEventFailure("통신 중 200 외 기타 코드")
                    }
                }

            }

            override fun onFailure(call: Call<DeleteEventResponse>, t: Throwable) {
                Log.d("DeleteEvent", "OnFailure")
                deleteEventView.onDeleteEventFailure(t.message ?: "통신 오류")
            }
        })
    }
}