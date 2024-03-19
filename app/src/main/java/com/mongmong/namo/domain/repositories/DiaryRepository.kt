package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.diary.Diary
import java.io.File


interface DiaryRepository {
    suspend fun getDiary(localId: Long): Diary

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

    suspend fun postDiaryToServer(eventServerId: Long, eventId: Long)
}