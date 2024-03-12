package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.local.entity.diary.Diary
import java.io.File


interface DiaryRepository {
    suspend fun getDiary(localId: Long)

    suspend fun addDiary(
        diary: Diary,
        diaryLocalId: Long,
        content: String,
        images: List<File>?,
        serverId: Long
    )

    suspend fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    )

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(eventServerId: Long, eventId: Long)
}