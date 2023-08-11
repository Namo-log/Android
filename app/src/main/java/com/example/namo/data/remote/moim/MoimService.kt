package com.example.namo.data.remote.moim

import android.util.Log
import com.example.namo.config.ApplicationClass
import com.example.namo.config.BaseResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoimService {
    private val moimRetrofitInterface : MoimRetrofitInterface =
        ApplicationClass.sRetrofit.create(MoimRetrofitInterface::class.java)

    private lateinit var addMoimView : AddMoimView
    private lateinit var getMoimListView : GetMoimListView
    private lateinit var getMoimScheduleView : GetMoimScheduleView
    private lateinit var deleteMoimMemberView : DeleteMoimMemberView
    private lateinit var participateMoimView: ParticipateMoimView

    fun setAddMoimView(addMoimView: AddMoimView) {
        this.addMoimView = addMoimView
    }

    fun setGetMoimListView(getMoimListView: GetMoimListView) {
        this.getMoimListView = getMoimListView
    }

    fun setGetMoimScheduleView(getMoimScheduleView: GetMoimScheduleView) {
        this.getMoimScheduleView = getMoimScheduleView
    }

    fun setDeleteMoimMemberView(deleteMoimMemberView: DeleteMoimMemberView) {
        this.deleteMoimMemberView = deleteMoimMemberView
    }

    fun setParticipateMoimView (participateMoimView: ParticipateMoimView) {
        this.participateMoimView = participateMoimView
    }

    fun addMoim(
        img : MultipartBody.Part,
        groupName : RequestBody
    ) {
        moimRetrofitInterface.addMoim(img, groupName)
            .enqueue(object : Callback<AddMoimResponse> {
                override fun onResponse(
                    call: Call<AddMoimResponse>,
                    response: Response<AddMoimResponse>
                ) {
                    when(response.code()) {
                        200 -> addMoimView.onAddMoimSuccess(response.body() as AddMoimResponse)
                        else -> {
                            Log.d("AddMoim", "Success but error")
                            addMoimView.onAddMoimFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<AddMoimResponse>, t: Throwable) {
                    Log.d("AddMoim", "onFailure")
                    addMoimView.onAddMoimFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun getMoimList() {
        moimRetrofitInterface.getMoimList()
            .enqueue(object : Callback<GetMoimListResponse> {
                override fun onResponse(
                    call: Call<GetMoimListResponse>,
                    response: Response<GetMoimListResponse>
                ) {
                    when(response.code()) {
                        200 -> getMoimListView.onGetMoimListSuccess(response.body() as GetMoimListResponse)
                        else -> {
                            Log.d("GetMoimList", "Success but error")
                            getMoimListView.onGetMoimListFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetMoimListResponse>, t: Throwable) {
                    Log.d("GetMoimList", "onFailure")
                    getMoimListView.onGetMoimListFailure(t.message ?: "통신 오류")
                }

            })
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

    fun participateMoim(
        groupCode : String
    ) {
        moimRetrofitInterface.participateMoim(groupCode)
            .enqueue(object : Callback<ParticipateMoimResponse> {
                override fun onResponse(
                    call: Call<ParticipateMoimResponse>,
                    response: Response<ParticipateMoimResponse>
                ) {
                    if (response.isSuccessful) {
                        participateMoimView.onParticipateMoimSuccess(response as ParticipateMoimResponse)
                    } else {
                        if (response.errorBody() != null) {
                            participateMoimView.onParticipateMoimFailure(response.errorBody()?.string().toString())
                        } else {
                            participateMoimView.onParticipateMoimFailure("Success but failure")
                        }
                    }
                }

                override fun onFailure(call: Call<ParticipateMoimResponse>, t: Throwable) {
                    Log.d("ParticipateMoim", "onFailure")
                    participateMoimView.onParticipateMoimFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun deleteMoimMember(
        moimId: Long
    ) {
        moimRetrofitInterface.deleteMoimMember(moimId)
            .enqueue(object : Callback<BaseResponse> {
                override fun onResponse(
                    call: Call<BaseResponse>,
                    response: Response<BaseResponse>
                ) {
                    when(response.code()) {
                        200 -> deleteMoimMemberView.onDeleteMoimMemberSuccess(response.body() as BaseResponse)
                        else -> {
                            Log.d("DeleteMoimMember", "Success but error")
                            deleteMoimMemberView.onDeleteMoimMemberFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                    Log.d("DeleteMoimMember", "onFailure")
                    deleteMoimMemberView.onDeleteMoimMemberFailure(t.message ?: "통신 오류")
                }

            })
    }

    fun updateMoimName(
        body: UpdateMoimNameBody
    ) {
        moimRetrofitInterface.updateMoimName(body)
            .enqueue(object : Callback<AddMoimResponse> {
                override fun onResponse(
                    call: Call<AddMoimResponse>,
                    response: Response<AddMoimResponse>
                ) {
                    when(response.code()) {
                        200 -> deleteMoimMemberView.onUpdateMoimNameSuccess(response.body() as AddMoimResponse)
                        else -> {
                            Log.d("UpdateMoimName", "Success but error")
                            deleteMoimMemberView.onDeleteMoimMemberFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<AddMoimResponse>, t: Throwable) {
                    Log.d("UpdateMoimName", "onFailure")
                    deleteMoimMemberView.onUpdateMoimNameFailure(t.message ?: "통신 오류")
                }
            })
    }
}