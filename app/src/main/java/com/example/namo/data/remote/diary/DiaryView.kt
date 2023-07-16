package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(result: DiaryResponse.GetScheduleIdx, localId: Int)
    fun onAddDiaryFailure(localId: Int, serverId: Int)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(result: String, localId: Int,serverId: Int)
    fun onDeleteDiarySuccess(localId: Int,serverId: Int)
    fun onEditDiaryFailure(localId: Int,serverId: Int)
    fun onDeleteDiaryFailure(localId: Int,serverId: Int)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(
        result: List<DiaryResponse.MonthDiaryDto>
    )

    fun onGetMonthDiaryFailure()
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess(localId: Int, result: DiaryResponse.DayDiaryDto)
    fun onGetDayDiaryFailure(localId: Int)
}

