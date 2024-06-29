package com.mongmong.namo.domain.repositories

import androidx.paging.PagingSource
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.GetMoimMemoResponse
import com.mongmong.namo.domain.model.GetPersonalDiaryResponse
import com.mongmong.namo.domain.model.group.MoimDiaryResult


interface DiaryRepository {
    suspend fun getPersonalDiary(localId: Long): GetPersonalDiaryResponse

    suspend fun addPersonalDiary(
        diary: Diary,
        images: List<String>?
    ): DiaryAddResponse

    suspend fun editPersonalDiary(
        diary: Diary,
        images: List<String>?
    ): DiaryResponse

    suspend fun deletePersonalDiary(
        scheduleId: Long
    ): DiaryResponse

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(serverId: Long, scheduleId: Long)

    fun getPersonalDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    fun getMoimDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult

    suspend fun getMoimMemo(scheduleId: Long): GetMoimMemoResponse

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