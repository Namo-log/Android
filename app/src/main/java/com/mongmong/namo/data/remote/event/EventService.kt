package com.mongmong.namo.data.remote.event

import android.util.Log
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.data.local.entity.home.EventForUpload
import com.mongmong.namo.domain.model.DeleteEventResponse
import com.mongmong.namo.domain.model.EditEventResponse
import com.mongmong.namo.domain.model.GetMonthEventResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventService {
    private lateinit var eventView : EventView
    private lateinit var deleteEventView : DeleteEventView
    private lateinit var getMonthEventView : GetMonthEventView
    private lateinit var getAllEventView : GetAllEventView
    private lateinit var getAllMoimEventView: GetAllMoimEventView
    private lateinit var getMonthMoimEventView : GetMonthMoimEventView

    fun setEventView(eventView : EventView) {
        this.eventView = eventView
    }

    fun setDeleteEventView(deleteEventView: DeleteEventView) {
        this.deleteEventView = deleteEventView
    }

    fun setGetMonthEventView(getMonthEventView: GetMonthEventView) {
        this.getMonthEventView = getMonthEventView
    }

    fun setGetAllEventView(getAllEventView: GetAllEventView) {
        this.getAllEventView = getAllEventView
    }

    fun setGetAllMoimEventView(getAllMoimEventView: GetAllMoimEventView) {
        this.getAllMoimEventView = getAllMoimEventView
    }

    fun setGetMonthMoimEventView(getMonthMoimEventView: GetMonthMoimEventView) {
        this.getMonthMoimEventView = getMonthMoimEventView
    }

    val eventRetrofitInterface = ApplicationClass.sRetrofit.create(EventRetrofitInterface::class.java)

    fun postEvent(body : EventForUpload, scheduleId : Long) {
        /*
        eventRetrofitInterface.postEvent(body).enqueue(object : Callback<PostEventResponse> {
            override fun onResponse(call: Call<PostEventResponse>, response: Response<PostEventResponse>) {
                when (response.code()) {
                    200 -> eventView.onPostEventSuccess(response.body() as PostEventResponse, scheduleId)
                    else -> {
                        Log.d("PostEvent", "Success but error")
                        eventView.onPostEventFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<PostEventResponse>, t: Throwable) {
                Log.d("PostEvent", "onFailure")
                eventView.onPostEventFailure(t.message ?: "통신 오류")
            }
        })
        */
    }

    fun editEvent(serverId : Long, body: EventForUpload, scheduleId : Long) {
        /*
        Log.d("EditEvent", "serverId : $serverId")
        eventRetrofitInterface.editEvent(serverId, body).enqueue(object : Callback<EditEventResponse> {
            override fun onResponse(call: Call<EditEventResponse>, response: Response<EditEventResponse>) {
                when (response.code()) {
                    200 -> eventView.onEditEventSuccess(response.body() as EditEventResponse, scheduleId)
                    else -> {
                        Log.d("EditEvent", "Success but error")
                        eventView.onEditEventFailure("통신 중 200 외 기타 코드")
                    }
                }

            }

            override fun onFailure(call: Call<EditEventResponse>, t: Throwable) {
                Log.d("EditEvent", "OnFailure")
                eventView.onEditEventFailure(t.message ?: "통신 오류")
            }
        })
         */
    }

    fun deleteEvent(serverId: Long, scheduleId : Long, isMoimSchedule: Int) {
        /*
        eventRetrofitInterface.deleteEvent(serverId, isMoimSchedule).enqueue(object : Callback<DeleteEventResponse> {
            override fun onResponse(call: Call<DeleteEventResponse>, response: Response<DeleteEventResponse>) {
                when (response.code()) {
                    200 -> deleteEventView.onDeleteEventSuccess(response.body() as DeleteEventResponse, scheduleId)
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
         */
    }

    fun getMonthEvent(yearMonth : String) {
        eventRetrofitInterface.getMonthEvent(yearMonth)
            .enqueue(object : Callback<GetMonthEventResponse> {
                override fun onResponse(
                    call: Call<GetMonthEventResponse>,
                    response: Response<GetMonthEventResponse>
                ) {
                    when (response.code()) {
                        200 -> getMonthEventView.onGetMonthEventSuccess(response.body() as GetMonthEventResponse)
                        else -> {
                            Log.d("GetMonthEvent", "Success but error")
                            getMonthEventView.onGetMonthEventFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthEventResponse>, t: Throwable) {
                    Log.d("GetMonthEvent", "OnFailure")
                    getMonthEventView.onGetMonthEventFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getAllEvent() {
        eventRetrofitInterface.getAllEvent()
            .enqueue(object : Callback<GetMonthEventResponse> {
                override fun onResponse(
                    call: Call<GetMonthEventResponse>,
                    response: Response<GetMonthEventResponse>
                ) {
                    when (response.code()) {
                        200 -> getAllEventView.onGetAllEventSuccess(response.body() as GetMonthEventResponse)
                        else -> {
                            Log.d("GetAllEvent", "Success but error")
                            getAllEventView.onGetAllEventFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthEventResponse>, t: Throwable) {
                    Log.d("GetMonthEvent", "OnFailure")
                    getAllEventView.onGetAllEventFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getAllMoimEvent() {
        eventRetrofitInterface.getAllMoimEvent()
            .enqueue(object : Callback<GetMonthEventResponse> {
                override fun onResponse(
                    call: Call<GetMonthEventResponse>,
                    response: Response<GetMonthEventResponse>
                ) {
                    when (response.code()) {
                        200 -> getAllMoimEventView.onGetAllMoimEventSuccess(response.body() as GetMonthEventResponse)
                        else -> {
                            Log.d("GetAllMoimEvent", "Success but error")
                            getAllMoimEventView.onGetAllMoimEventFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthEventResponse>, t: Throwable) {
                    Log.d("GetAllMoimEvent", "OnFailure")
                    getAllMoimEventView.onGetAllMoimEventFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getMonthMoimEvent(yearMonth : String) {
        eventRetrofitInterface.getMonthMoimEvent(yearMonth)
            .enqueue(object : Callback<GetMonthEventResponse> {
                override fun onResponse(
                    call: Call<GetMonthEventResponse>,
                    response: Response<GetMonthEventResponse>
                ) {
                    when (response.code()) {
                        200 -> getMonthMoimEventView.onGetMonthMoimEventSuccess(response.body() as GetMonthEventResponse)
                        else -> {
                            Log.d("GetMonthMoimEvent", "Success but error")
                            getMonthMoimEventView.onGetMonthMoimEventFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthEventResponse>, t: Throwable) {
                    Log.d("GetMonthMoimEvent", "OnFailure")
                    getMonthMoimEventView.onGetMonthMoimEventFailure(t.message ?: "통신 오류")
                }

            })
    }
}