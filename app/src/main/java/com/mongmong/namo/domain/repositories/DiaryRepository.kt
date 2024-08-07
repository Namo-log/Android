package com.mongmong.namo.domain.repositories

import androidx.paging.PagingSource
import com.mongmong.namo.domain.model.PersonalDiary
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.GetPersonalDiaryResponse
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.group.MoimDiaryResult


interface DiaryRepository {
    suspend fun getPersonalDiary(localId: Long): GetPersonalDiaryResponse

    suspend fun addPersonalDiary(
        diary: PersonalDiary,
        images: List<String>
    ): DiaryResponse

    suspend fun editPersonalDiary(
        diary: PersonalDiary,
        images: List<String>,
        deleteImageIds: List<Long>?
    ): DiaryResponse

    suspend fun deletePersonalDiary(
        scheduleId: Long
    ): DiaryResponse

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(serverId: Long, scheduleId: Long)

    fun getPersonalDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    fun getMoimDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

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