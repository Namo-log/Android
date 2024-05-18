package com.mongmong.namo.domain.repositories

import androidx.paging.PagingSource
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.GetDiaryResponse
import com.mongmong.namo.domain.model.GetDiaryResult
import com.mongmong.namo.domain.model.group.MoimDiaryResult


interface DiaryRepository {
    suspend fun getDiary(localId: Long): Diary

    suspend fun addDiary(
        diary: Diary,
        images: List<String>?
    )

    suspend fun editDiary(
        diary: Diary,
        images: List<String>?
    )

    suspend fun deleteDiary(
        localId: Long,
        scheduleServerId: Long
    )

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(serverId: Long, scheduleId: Long)

    fun getPersonalDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    fun getMoimDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult

    suspend fun getMoimMemo(scheduleId: Long): GetDiaryResponse

    suspend fun patchMoimMemo(scheduleId: Long, content: String): Boolean

    suspend fun deleteMoimMemo(scheduleId: Long): Boolean

    suspend fun addMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    )

    suspend fun editMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    )

    suspend fun deleteMoimActivity(activityId: Long)

    suspend fun deleteMoimDiary(moimScheduleId: Long): Boolean
}