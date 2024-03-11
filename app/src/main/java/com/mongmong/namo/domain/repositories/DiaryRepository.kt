package com.mongmong.namo.domain.repositories

import com.mongmong.namo.data.remote.diary.DiaryResponse

interface DiaryRepository {
    suspend fun addDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    )

    suspend fun addDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    )

    suspend fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    )

    suspend fun editDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    )

    suspend fun getDiary(localId: Long)

    suspend fun uploadDiaryToServer()

    suspend fun postDiaryToServer(eventServerId: Long, eventId: Long)
}