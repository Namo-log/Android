package com.example.namo.data.remote.moim

import com.example.namo.config.BaseResponse

interface AddMoimView {
    fun onAddMoimSuccess(response: AddMoimResponse)
    fun onAddMoimFailure(message : String)
}

interface GetMoimListView {
    fun onGetMoimListSuccess(response: GetMoimListResponse)
    fun onGetMoimListFailure(message : String)
}

interface GetMoimScheduleView {
    fun onGetMoimScheduleSuccess(response : GetMoimScheduleResponse)
    fun onGetMoimScheduleFailure(message: String)
}

interface DeleteMoimMemberView {
    fun onUpdateMoimNameSuccess(response: AddMoimResponse)
    fun onUpdateMoimNameFailure(message: String)
    fun onDeleteMoimMemberSuccess(response: BaseResponse)
    fun onDeleteMoimMemberFailure(message: String)
}

interface ParticipateMoimView {
    fun onParticipateMoimSuccess(response: ParticipateMoimResponse)
    fun onParticipateMoimFailure(message: String)
}

interface MoimScheduleView {
    fun onAddMoimScheduleSuccess(response : AddMoimScheduleResponse)
    fun onAddMoimScheduleFailure(message: String)

    fun onEditMoimScheduleSuccess(message: String)
    fun onEditMoimScheduleFailure(message: String)

    fun onDeleteMoimScheduleSuccess(message: String)
    fun onDeleteMoimScheduleFailure(message: String)
}