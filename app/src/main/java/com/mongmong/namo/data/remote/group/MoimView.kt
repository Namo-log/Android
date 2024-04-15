package com.mongmong.namo.data.remote.group

import com.mongmong.namo.domain.model.AddGroupResponse
import com.mongmong.namo.domain.model.AddMoimScheduleResponse
import com.mongmong.namo.domain.model.GetGroupsResponse
import com.mongmong.namo.domain.model.GetMoimScheduleResponse
import com.mongmong.namo.domain.model.JoinGroupResponse
import com.mongmong.namo.presentation.config.BaseResponse

interface AddMoimView {
    fun onAddMoimSuccess(response: AddGroupResponse)
    fun onAddMoimFailure(message : String)
}

interface GetMoimListView {
    fun onGetMoimListSuccess(response: GetGroupsResponse)
    fun onGetMoimListFailure(message : String)
}

interface GetMoimScheduleView {
    fun onGetMoimScheduleSuccess(response : GetMoimScheduleResponse)
    fun onGetMoimScheduleFailure(message: String)
}

interface DeleteMoimMemberView {
    fun onUpdateMoimNameSuccess(response: JoinGroupResponse)
    fun onUpdateMoimNameFailure(message: String)
    fun onDeleteMoimMemberSuccess(response: BaseResponse)
    fun onDeleteMoimMemberFailure(message: String)
}

interface ParticipateMoimView {
    fun onParticipateMoimSuccess(response: JoinGroupResponse)
    fun onParticipateMoimFailure(message: String)
}

interface MoimScheduleView {
//    fun onAddMoimScheduleSuccess(response : AddMoimScheduleResponse)
//    fun onAddMoimScheduleFailure(message: String)

//    fun onEditMoimScheduleSuccess(message: String)
//    fun onEditMoimScheduleFailure(message: String)

//    fun onDeleteMoimScheduleSuccess(message: String)
//    fun onDeleteMoimScheduleFailure(message: String)
}

interface EditMoimScheduleView {
    fun onPatchMoimScheduleCategorySuccess(message: String)
    fun onPatchMoimScheduleCategoryFailure(message: String)
    fun onPostMoimScheduleAlarmSuccess(message: String)
    fun onPostMoimScheduleAlarmFailure(message: String)
    fun onPatchMoimScheduleAlarmSuccess(message: String)
    fun onPatchMoimScheduleAlarmFailure(message: String)
}