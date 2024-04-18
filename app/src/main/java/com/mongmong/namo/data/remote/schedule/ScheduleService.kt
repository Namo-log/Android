package com.mongmong.namo.data.remote.schedule

import android.util.Log
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.ScheduleRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ScheduleService {
    private lateinit var eventView : ScheduleView
    private lateinit var deleteScheduleView : DeleteScheduleView
    private lateinit var getMonthScheduleView : GetMonthScheduleView
    private lateinit var getAllScheduleView : GetAllScheduleView
    private lateinit var getAllMoimScheduleView: GetAllMoimScheduleView
    private lateinit var getMonthMoimScheduleView : GetMonthMoimScheduleView

    fun setScheduleView(eventView : ScheduleView) {
        this.eventView = eventView
    }

    fun setDeleteScheduleView(deleteScheduleView: DeleteScheduleView) {
        this.deleteScheduleView = deleteScheduleView
    }

    fun setGetMonthScheduleView(getMonthScheduleView: GetMonthScheduleView) {
        this.getMonthScheduleView = getMonthScheduleView
    }

    fun setGetAllScheduleView(getAllScheduleView: GetAllScheduleView) {
        this.getAllScheduleView = getAllScheduleView
    }

    fun setGetAllMoimScheduleView(getAllMoimScheduleView: GetAllMoimScheduleView) {
        this.getAllMoimScheduleView = getAllMoimScheduleView
    }

    fun setGetMonthMoimScheduleView(getMonthMoimScheduleView: GetMonthMoimScheduleView) {
        this.getMonthMoimScheduleView = getMonthMoimScheduleView
    }

    val eventRetrofitInterface = ApplicationClass.sRetrofit.create(ScheduleApiService::class.java)

    fun postSchedule(body : ScheduleRequestBody, scheduleId : Long) {
        /*
        eventRetrofitInterface.postSchedule(body).enqueue(object : Callback<PostScheduleResponse> {
            override fun onResponse(call: Call<PostScheduleResponse>, response: Response<PostScheduleResponse>) {
                when (response.code()) {
                    200 -> eventView.onPostScheduleSuccess(response.body() as PostScheduleResponse, scheduleId)
                    else -> {
                        Log.d("PostSchedule", "Success but error")
                        eventView.onPostScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<PostScheduleResponse>, t: Throwable) {
                Log.d("PostSchedule", "onFailure")
                eventView.onPostScheduleFailure(t.message ?: "통신 오류")
            }
        })
        */
    }

    fun editSchedule(serverId : Long, body: ScheduleRequestBody, scheduleId : Long) {
        /*
        Log.d("EditSchedule", "serverId : $serverId")
        eventRetrofitInterface.editSchedule(serverId, body).enqueue(object : Callback<EditScheduleResponse> {
            override fun onResponse(call: Call<EditScheduleResponse>, response: Response<EditScheduleResponse>) {
                when (response.code()) {
                    200 -> eventView.onEditScheduleSuccess(response.body() as EditScheduleResponse, scheduleId)
                    else -> {
                        Log.d("EditSchedule", "Success but error")
                        eventView.onEditScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }

            }

            override fun onFailure(call: Call<EditScheduleResponse>, t: Throwable) {
                Log.d("EditSchedule", "OnFailure")
                eventView.onEditScheduleFailure(t.message ?: "통신 오류")
            }
        })
         */
    }

    fun deleteSchedule(serverId: Long, scheduleId : Long, isMoimSchedule: Int) {
        /*
        eventRetrofitInterface.deleteSchedule(serverId, isMoimSchedule).enqueue(object : Callback<DeleteScheduleResponse> {
            override fun onResponse(call: Call<DeleteScheduleResponse>, response: Response<DeleteScheduleResponse>) {
                when (response.code()) {
                    200 -> deleteScheduleView.onDeleteScheduleSuccess(response.body() as DeleteScheduleResponse, scheduleId)
                    else -> {
                        Log.d("DeleteSchedule", "Success but error")
                        deleteScheduleView.onDeleteScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }

            }

            override fun onFailure(call: Call<DeleteScheduleResponse>, t: Throwable) {
                Log.d("DeleteSchedule", "OnFailure")
                deleteScheduleView.onDeleteScheduleFailure(t.message ?: "통신 오류")
            }
        })
         */
    }

    fun getMonthSchedule(yearMonth : String) {
        eventRetrofitInterface.getMonthSchedule(yearMonth)
            .enqueue(object : Callback<GetMonthScheduleResponse> {
                override fun onResponse(
                    call: Call<GetMonthScheduleResponse>,
                    response: Response<GetMonthScheduleResponse>
                ) {
                    when (response.code()) {
                        200 -> getMonthScheduleView.onGetMonthScheduleSuccess(response.body() as GetMonthScheduleResponse)
                        else -> {
                            Log.d("GetMonthSchedule", "Success but error")
                            getMonthScheduleView.onGetMonthScheduleFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthScheduleResponse>, t: Throwable) {
                    Log.d("GetMonthSchedule", "OnFailure")
                    getMonthScheduleView.onGetMonthScheduleFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getAllSchedule() {
        eventRetrofitInterface.getAllSchedule()
            .enqueue(object : Callback<GetMonthScheduleResponse> {
                override fun onResponse(
                    call: Call<GetMonthScheduleResponse>,
                    response: Response<GetMonthScheduleResponse>
                ) {
                    when (response.code()) {
                        200 -> getAllScheduleView.onGetAllScheduleSuccess(response.body() as GetMonthScheduleResponse)
                        else -> {
                            Log.d("GetAllSchedule", "Success but error")
                            getAllScheduleView.onGetAllScheduleFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthScheduleResponse>, t: Throwable) {
                    Log.d("GetMonthSchedule", "OnFailure")
                    getAllScheduleView.onGetAllScheduleFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getAllMoimSchedule() {
        eventRetrofitInterface.getAllMoimSchedule()
            .enqueue(object : Callback<GetMonthScheduleResponse> {
                override fun onResponse(
                    call: Call<GetMonthScheduleResponse>,
                    response: Response<GetMonthScheduleResponse>
                ) {
                    when (response.code()) {
                        200 -> getAllMoimScheduleView.onGetAllMoimScheduleSuccess(response.body() as GetMonthScheduleResponse)
                        else -> {
                            Log.d("GetAllMoimSchedule", "Success but error")
                            getAllMoimScheduleView.onGetAllMoimScheduleFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMonthScheduleResponse>, t: Throwable) {
                    Log.d("GetAllMoimSchedule", "OnFailure")
                    getAllMoimScheduleView.onGetAllMoimScheduleFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getMonthMoimSchedule(yearMonth : String) {
//        eventRetrofitInterface.getMonthMoimSchedule(yearMonth)
//            .enqueue(object : Callback<GetMonthScheduleResponse> {
//                override fun onResponse(
//                    call: Call<GetMonthScheduleResponse>,
//                    response: Response<GetMonthScheduleResponse>
//                ) {
//                    when (response.code()) {
//                        200 -> getMonthMoimScheduleView.onGetMonthMoimScheduleSuccess(response.body() as GetMonthScheduleResponse)
//                        else -> {
//                            Log.d("GetMonthMoimSchedule", "Success but error")
//                            getMonthMoimScheduleView.onGetMonthMoimScheduleFailure("통신 중 200 외 기타 코드")
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<GetMonthScheduleResponse>, t: Throwable) {
//                    Log.d("GetMonthMoimSchedule", "OnFailure")
//                    getMonthMoimScheduleView.onGetMonthMoimScheduleFailure(t.message ?: "통신 오류")
//                }
//
//            })
    }
}