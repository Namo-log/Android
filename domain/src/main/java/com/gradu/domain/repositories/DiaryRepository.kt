package com.gradu.domain.repositories

import com.gradu.domain.model.BaseResponse
import com.gradu.domain.model.CalendarDiaryDate
import com.gradu.domain.model.Diary
import com.gradu.domain.model.DiaryDetail
import com.gradu.domain.model.MoimPayment
import com.gradu.domain.model.ScheduleForDiary
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    /** 기록 */
    // 기록 보관함 조회
    fun getDiaryArchivePagingSource(
        filterType: String?,
        keyword: String?,
    ): Flow<List<Diary>>

    // 기록 일정 정보 조회
    suspend fun getScheduleForDiary(scheduleId: Long): ScheduleForDiary

    // 기록 개별 조회
    suspend fun getDiary(scheduleId: Long): DiaryDetail

    // 기록 추가
    suspend fun addDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): BaseResponse

    // 기록 수정
    suspend fun editDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): BaseResponse

    // 기록 삭제
    suspend fun deleteDiary(diaryId: Long): BaseResponse

    // 기록 캘린더 조회
    suspend fun getCalendarDiary(yearMonth: String): CalendarDiaryDate

    // 날짜별 기록 조회 (기록 캘린더)
    suspend fun getDiaryByDate(date: String): List<Diary>

    suspend fun getMoimPayment(scheduleId: Long): MoimPayment
}