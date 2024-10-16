package com.mongmong.namo.domain.repositories

import androidx.paging.PagingData
import com.mongmong.namo.domain.model.CalendarDiaryDate
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.MoimPayment
import com.mongmong.namo.domain.model.ScheduleForDiary
import kotlinx.coroutines.flow.Flow


interface DiaryRepository {
    /** 기록 */
    // 기록 보관함 조회
    fun getDiaryCollectionPagingSource(
        filterType: String?,
        keyword: String?,
    ): Flow<PagingData<Diary>>

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
    ): DiaryBaseResponse

    // 기록 수정
    suspend fun editDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): DiaryBaseResponse

    // 기록 삭제
    suspend fun deleteDiary(diaryId: Long): DiaryBaseResponse

    // 기록 캘린더 조회
    suspend fun getCalendarDiary(yearMonth: String): CalendarDiaryDate

    // 날짜별 기록 조회 (기록 캘린더)
    suspend fun getDiaryByDate(date: String): List<Diary>

    suspend fun getMoimPayment(scheduleId: Long): MoimPayment
}