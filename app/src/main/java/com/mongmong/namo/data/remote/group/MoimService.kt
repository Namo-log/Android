package com.mongmong.namo.data.remote.group

import android.util.Log
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.domain.model.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.model.UpdateGroupNameRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoimService {
    private val moimRetrofitInterface : GroupApiService =
        ApplicationClass.sRetrofit.create(GroupApiService::class.java)

    private lateinit var getMoimScheduleView : GetMoimScheduleView
    private lateinit var moimScheduleView : MoimScheduleView
    private lateinit var editMoimScheduleView : EditMoimScheduleView


    fun setGetMoimScheduleView(getMoimScheduleView: GetMoimScheduleView) {
        this.getMoimScheduleView = getMoimScheduleView
    }


    fun setMoimScheduleView (moimScheduleView : MoimScheduleView) {
        this.moimScheduleView = moimScheduleView
    }

    fun setEditMoimScheduleView (editMoimScheduleView: EditMoimScheduleView) {
        this.editMoimScheduleView = editMoimScheduleView
    }

    fun getMoimSchedule(
        moimId : Long,
        yearMonth : String
    ) {
        moimRetrofitInterface.getMoimSchedule(moimId, yearMonth)
            .enqueue(object : Callback<GetMoimScheduleResponse> {
                override fun onResponse(
                    call: Call<GetMoimScheduleResponse>,
                    response: Response<GetMoimScheduleResponse>
                ) {
                    when(response.code()) {
                        200 -> getMoimScheduleView.onGetMoimScheduleSuccess(response.body() as GetMoimScheduleResponse)
                        else -> {
                            Log.d("GetMoimSchedule", "Success but error")
                            getMoimScheduleView.onGetMoimScheduleFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMoimScheduleResponse>, t: Throwable) {
                    Log.d("GetMoimSchedule", "onFailure")
                    getMoimScheduleView.onGetMoimScheduleFailure(t.message ?: "통신 오류")
                }

            })
    }



    fun postMoimSchedule(body : AddMoimSchedule) {
        moimRetrofitInterface.postMoimSchedule(body).enqueue(object : Callback<AddMoimScheduleResponse> {
            override fun onResponse(
                call: Call<AddMoimScheduleResponse>,
                response: Response<AddMoimScheduleResponse>
            ) {
                when(response.code()) {
                    200 -> moimScheduleView.onAddMoimScheduleSuccess(response.body() as AddMoimScheduleResponse)
                    else -> {
                        Log.d("PostMoimSchedule", "Success but error")
                        moimScheduleView.onAddMoimScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<AddMoimScheduleResponse>, t: Throwable) {
                Log.d("PostMoimSchedule", "onFailure")
                moimScheduleView.onAddMoimScheduleFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun editMoimSchedule(body : EditMoimSchedule) {
        moimRetrofitInterface.editMoimSchedule(body).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                when(response.code()) {
                    200 -> moimScheduleView.onEditMoimScheduleSuccess(response.message())
                    else -> {
                        Log.d("EditMoimSchedule", "Success but error")
                        moimScheduleView.onEditMoimScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.d("EditMoimSchedule", "onFailure")
                moimScheduleView.onEditMoimScheduleFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun deleteMoimSchedule(moimScheduleId : Long) {
        moimRetrofitInterface.deleteMoimSchedule(moimScheduleId).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                when(response.code()) {
                    200 -> moimScheduleView.onDeleteMoimScheduleSuccess(response.message())
                    else -> {
                        Log.d("DeleteMoimSchedule", "Success but error")
                        moimScheduleView.onDeleteMoimScheduleFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.d("DeleteMoimSchedule", "onFailure")
                moimScheduleView.onDeleteMoimScheduleFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun patchMoimScheduleCategory(body : PatchMoimScheduleCategoryBody) {
        moimRetrofitInterface.patchMoimScheduleCategory(body).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                when(response.code()) {
                    200 -> editMoimScheduleView.onPatchMoimScheduleCategorySuccess(response.message())
                    else -> {
                        Log.d("PatchMoimScheduleCategory", "Success but error")
                        editMoimScheduleView.onPatchMoimScheduleCategoryFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.d("PatchMoimScheduleCategory", "onFailure")
                editMoimScheduleView.onPatchMoimScheduleCategoryFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun postMoimScheduleAlarm(body : MoimScheduleAlarmBody) {
        moimRetrofitInterface.postMoimScheduleAlarm(body).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                when(response.code()) {
                    200 -> editMoimScheduleView.onPostMoimScheduleAlarmSuccess(response.message())
                    else -> {
                        Log.d("PostMoimScheduleAlarm", "Success but error")
                        editMoimScheduleView.onPostMoimScheduleAlarmFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.d("PostMoimScheduleAlarm", "onFailure")
                editMoimScheduleView.onPostMoimScheduleAlarmFailure(t.message ?: "통신 오류")
            }
        })
    }

    fun patchMoimScheduleAlarm(body : MoimScheduleAlarmBody) {
        moimRetrofitInterface.patchMoimScheduleAlarm(body).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(
                call: Call<BaseResponse>,
                response: Response<BaseResponse>
            ) {
                when(response.code()) {
                    200 -> editMoimScheduleView.onPatchMoimScheduleAlarmSuccess(response.message())
                    else -> {
                        Log.d("PatchMoimScheduleAlarm", "Success but error")
                        editMoimScheduleView.onPatchMoimScheduleAlarmFailure("통신 중 200 외 기타 코드")
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.d("PatchMoimScheduleAlarm", "onFailure")
                editMoimScheduleView.onPatchMoimScheduleAlarmFailure(t.message ?: "통신 오류")
            }
        })
    }
}