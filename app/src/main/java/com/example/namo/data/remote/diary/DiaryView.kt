package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(result: DiaryResponse.GetScheduleIdx, localId: Long)
    fun onAddDiaryFailure(localId: Long,message:String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(result: String, localId: Long)
    fun onDeleteDiarySuccess(localId: Long)
    fun onEditDiaryFailure(localId: Long,message:String)
    fun onDeleteDiaryFailure(localId: Long,message:String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(
        result: List<DiaryResponse.Result>
    )

    fun onGetMonthDiaryFailure(yearMonth:String, message: String)
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess( result: DiaryResponse.DayDiaryDto,serverId: Long)
    fun onGetDayDiaryFailure(localId: Long,message:String)
}

