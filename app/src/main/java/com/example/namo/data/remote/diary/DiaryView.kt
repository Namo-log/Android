package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(response: DiaryResponse.DiaryAddResponse, localId: Long)
    fun onAddDiaryFailure(message: String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(response: DiaryResponse.DiaryEditResponse, localId: Long, serverId: Long)
    fun onEditDiaryFailure(message: String)
    fun onDeleteDiarySuccess(response: DiaryResponse.DiaryDeleteResponse, localId: Long)
    fun onDeleteDiaryFailure(message: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(response: DiaryResponse.DiaryGetAllResponse)
    fun onGetMonthDiaryFailure(message: String)
}

interface AddGroupDiaryView {
    fun onAddGroupDiarySuccess(response: DiaryResponse.AddGroupDiaryResponse)
    fun onAddGroupDiaryFailure(message: String)
}

interface GetGroupDiaryView {
    fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse)
    fun onGetGroupDiaryFailure(message: String)
}

