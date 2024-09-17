package com.mongmong.namo.domain.repositories

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.mongmong.namo.domain.model.CalendarDiaryDate
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.PersonalDiary
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import kotlinx.coroutines.flow.Flow


interface DiaryRepository {
    suspend fun getScheduleForDiary(scheduleId: Long): ScheduleForDiary
    suspend fun getPersonalDiary(localId: Long): DiaryDetail

    suspend fun addPersonalDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): DiaryResponse

    suspend fun editPersonalDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): DiaryResponse

    suspend fun deletePersonalDiary(
        scheduleId: Long
    ): DiaryResponse

    suspend fun getCalendarDiary(yearMonth: String): CalendarDiaryDate

    suspend fun getDiaryByDate(date: String): List<Diary>

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(serverId: Long, scheduleId: Long)

    fun getPersonalDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    fun getDiaryCollectionPagingSource(
        filterType: String?,
        keyword: String?,
    ): Flow<PagingData<Diary>>

    suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult

    suspend fun getMoimMemo(scheduleId: Long): MoimDiary

    suspend fun patchMoimMemo(scheduleId: Long, content: String): Boolean

    suspend fun deleteMoimMemo(scheduleId: Long): Boolean

    suspend fun addMoimActivity(
        moimScheduleId: Long,
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    )

    suspend fun editMoimActivity(
        activityId: Long,
        deleteImageIds: List<Long>?,
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    )

    suspend fun deleteMoimActivity(activityId: Long)

    suspend fun deleteMoimDiary(moimScheduleId: Long): Boolean
}