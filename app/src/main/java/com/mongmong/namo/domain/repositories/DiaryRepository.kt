package com.mongmong.namo.domain.repositories

import androidx.paging.PagingSource
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.MoimDiaryResult
import java.io.File


interface DiaryRepository {
    suspend fun getDiary(localId: Long): Diary
    suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult

    suspend fun addDiary(
        diary: Diary,
        images: List<File>?
    )

    suspend fun editDiary(
        diary: Diary,
        images: List<File>?
    )

    suspend fun deleteDiary(
        localId: Long,
        scheduleServerId: Long
    )

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(serverId: Long, scheduleId: Long)

    fun getPersonalDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>

    fun getMoimDiaryPagingSource(month: String): PagingSource<Int, DiarySchedule>
}