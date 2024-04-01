package com.mongmong.namo.data.remote.group

import android.util.Log
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.data.local.entity.group.AddMoimSchedule
import com.mongmong.namo.data.local.entity.group.EditMoimSchedule
import com.mongmong.namo.domain.model.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.MoimScheduleAlarmBody
import com.mongmong.namo.domain.model.JoinGroupResponse
import com.mongmong.namo.domain.model.PatchMoimScheduleCategoryBody
import com.mongmong.namo.domain.model.UpdateMoimNameBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MoimService {
    private val moimRetrofitInterface : GroupApiService =
        ApplicationClass.sRetrofit.create(GroupApiService::class.java)

    private lateinit var addMoimView : AddMoimView
    private lateinit var getMoimListView : GetMoimListView
    private lateinit var getMoimScheduleView : GetMoimScheduleView
    private lateinit var deleteMoimMemberView : DeleteMoimMemberView
    private lateinit var participateMoimView: ParticipateMoimView
    private lateinit var moimScheduleView : MoimScheduleView
    private lateinit var editMoimScheduleView : EditMoimScheduleView

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

    fun setMoimScheduleView (moimScheduleView : MoimScheduleView) {
        this.moimScheduleView = moimScheduleView
    }

    fun setEditMoimScheduleView (editMoimScheduleView: EditMoimScheduleView) {
        this.editMoimScheduleView = editMoimScheduleView
    }

    fun addMoim(
        img: MultipartBody.Part?,
        groupName: RequestBody
    ) {
        /*moimRetrofitInterface.addGroup(img, groupName)
            .enqueue(object : Callback<AddGroupResponse> {
                override fun onResponse(
                    call: Call<AddGroupResponse>,
                    response: Response<AddGroupResponse>
                ) {
                    when (response.code()) {
                        200 -> addMoimView.onAddMoimSuccess(response.body() as AddGroupResponse)
                        else -> {
                            Log.d("AddMoim", "Success but error")
                            addMoimView.onAddMoimFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<AddGroupResponse>, t: Throwable) {
                    Log.d("AddMoim", "onFailure")
                    addMoimView.onAddMoimFailure(t.message ?: "통신 오류")
                }

            })*/
    }

    fun getMoimList() {
        /*moimRetrofitInterface.getGroups()
            .enqueue(object : Callback<GetGroupsResponse> {
                override fun onResponse(
                    call: Call<GetGroupsResponse>,
                    response: Response<GetGroupsResponse>
                ) {
                    when(response.code()) {
                        200 -> getMoimListView.onGetMoimListSuccess(response.body() as GetGroupsResponse)
                        else -> {
                            Log.d("GetMoimList", "Success but error")
                            getMoimListView.onGetMoimListFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<GetGroupsResponse>, t: Throwable) {
                    Log.d("GetMoimList", "onFailure")
                    getMoimListView.onGetMoimListFailure(t.message ?: "통신 오류")
                }

            })*/
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
        /*moimRetrofitInterface.participateGroup(groupCode)
            .enqueue(object : Callback<JoinGroupResponse> {
                override fun onResponse(
                    call: Call<JoinGroupResponse>,
                    response: Response<JoinGroupResponse>
                ) {
                    when(response.code()) {
                        200 -> {
                            Log.d("ParticipateMoim","Code : 200")
                            participateMoimView.onParticipateMoimSuccess(response.body() as JoinGroupResponse)
                        }
                        else -> {
                            if (response.errorBody() != null) {
                                participateMoimView.onParticipateMoimFailure(response.errorBody()?.string().toString())
                            } else {
                                participateMoimView.onParticipateMoimFailure("Success but failure")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<JoinGroupResponse>, t: Throwable) {
                    Log.d("ParticipateMoim", "onFailure")
                    participateMoimView.onParticipateMoimFailure(t.message ?: "통신 오류")
                }

            })*/
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
            .enqueue(object : Callback<JoinGroupResponse> {
                override fun onResponse(
                    call: Call<JoinGroupResponse>,
                    response: Response<JoinGroupResponse>
                ) {
                    when(response.code()) {
                        200 -> deleteMoimMemberView.onUpdateMoimNameSuccess(response.body() as JoinGroupResponse)
                        else -> {
                            Log.d("UpdateMoimName", "Success but error")
                            deleteMoimMemberView.onDeleteMoimMemberFailure("통신 중 200 외 기타 코드")
                        }
                    }
                }

                override fun onFailure(call: Call<JoinGroupResponse>, t: Throwable) {
                    Log.d("UpdateMoimName", "onFailure")
                    deleteMoimMemberView.onUpdateMoimNameFailure(t.message ?: "통신 오류")
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