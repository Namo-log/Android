package com.example.namo.data.remote.diary

interface AddPersonalDiaryView {
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

interface GetGroupDiaryView {
    fun onGetGroupDiarySuccess(response: DiaryResponse.GetGroupDiaryResponse)
    fun onGetGroupDiaryFailure(message: String)
}


interface GetGroupMonthView {
    fun onGetGroupMonthSuccess(response: DiaryResponse.DiaryGetMonthResponse)
    fun onGetGroupMonthFailure(message: String)
}

interface AddGroupAfterDiaryView{
    fun onAddGroupAfterDiarySuccess(response: DiaryResponse.DiaryResponse)
    fun onAddGroupAfterDiaryFailure(message: String)
}

interface DiaryBasicView{
    fun onSuccess(response: DiaryResponse.DiaryResponse)
    fun onFailure(message: String)
}