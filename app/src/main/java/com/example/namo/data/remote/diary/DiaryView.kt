package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(result: DiaryResponse.GetScheduleIdx, localId: Long)
    fun onAddDiaryFailure(localId: Long, serverId: Long)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(result: String, localId: Long,serverId: Long)
    fun onDeleteDiarySuccess(localId: Long,serverId: Long)
    fun onEditDiaryFailure(localId: Long,serverId: Long)
    fun onDeleteDiaryFailure(localId: Long,serverId: Long)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(
        result: List<DiaryResponse.MonthDiaryDto>
    )

    fun onGetMonthDiaryFailure()
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess(localId: Long, result: DiaryResponse.DayDiaryDto)
    fun onGetDayDiaryFailure(localId: Long)
}

