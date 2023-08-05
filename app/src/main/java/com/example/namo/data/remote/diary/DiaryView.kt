package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(response: DiaryResponse.DiaryAddResponse, localId: Long)
    fun onAddDiaryFailure(localId: Long, message: String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(response: DiaryResponse.DiaryEditResponse, localId: Long)
    fun onEditDiaryFailure(localId: Long, message: String)
    fun onDeleteDiarySuccess(response: DiaryResponse.DiaryDeleteResponse, localId: Long)
    fun onDeleteDiaryFailure(localId: Long, message: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(response: DiaryResponse.DiaryGetMonthResponse)
    fun onGetMonthDiaryFailure(yearMonth: String, message: String)
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess(response: DiaryResponse.DiaryGetDayResponse, serverId: Long)
    fun onGetDayDiaryFailure(localId: Long, message: String)
}

