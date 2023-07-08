package com.example.namo.data.remote.diary

interface DiaryView {
    fun onAddDiarySuccess(code: Int, message: String, result: DiaryResponse.GetScheduleIdx)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(code: Int, message: String, result: String)
    fun onDeleteDiarySuccess(code: Int, message: String, result: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(
        code: Int,
        message: String,
        result: List<DiaryResponse.MonthDiaryDto>
    )
}

interface GetDayDiaryView {
    fun onGetDayDiarySuccess(code: Int, message: String, result: DiaryResponse.DayDiaryDto)
}

