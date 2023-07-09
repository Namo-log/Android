package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(code: Int, message: String, result: DiaryResponse.GetScheduleIdx)
    fun onAddDiaryFailure(message: String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(code: Int, message: String, result: String)
    fun onDeleteDiarySuccess(code: Int, message: String, result: String)
    fun onEditDiaryFailure(message: String)
    fun onDeleteDiaryFailure(message: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(
        code: Int,
        message: String,
        result: List<DiaryResponse.MonthDiaryDto>
    )

    fun onGetMonthDiaryFailure(message: String)
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess(code: Int, message: String, result: DiaryResponse.DayDiaryDto)
    fun onGetDayhDiaryFailure(message: String)
}

