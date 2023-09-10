package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(response: DiaryResponse.DiaryAddResponse, localId: Long)
    fun onAddDiaryFailure(message: String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(response: DiaryResponse.DiaryResponse, localId: Long, serverId: Long)
    fun onEditDiaryFailure(message: String)
    fun onDeleteDiarySuccess(response: DiaryResponse.DiaryResponse, localId: Long)
    fun onDeleteDiaryFailure(message: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(response: DiaryResponse.DiaryGetAllResponse)
    fun onGetMonthDiaryFailure(message: String)
}

interface AddGroupDiaryView {
    fun onAddGroupDiarySuccess(response: DiaryResponse.DiaryResponse)
    fun onAddGroupDiaryFailure(message: String)
}

interface GetGroupDiaryView {
    fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse)
    fun onGetGroupDiaryFailure(message: String)
}

interface EditGroupDiaryView {
    fun onEditGroupDiarySuccess(response: DiaryResponse.DiaryResponse)
    fun onEditGroupDiaryFailure(message: String)
}

interface DeleteGroupDiaryView {
    fun onDeleteGroupDiarySuccess(response: DiaryResponse.DiaryResponse)
    fun onDeleteGroupDiaryFailure(message: String)
}

interface GetGroupMonthView {
    fun onGetGroupMonthSuccess(response: DiaryResponse.DiaryGetMonthResponse)
    fun onGetGroupMonthFailure(message: String)
}
