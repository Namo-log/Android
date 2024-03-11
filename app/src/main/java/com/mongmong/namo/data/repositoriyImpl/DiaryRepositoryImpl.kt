package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.domain.repositories.DiaryRepository

class DiaryRepositoryImpl(private val diaryDao: DiaryDao): DiaryRepository {
    override suspend fun addDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun addDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun editDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getDiary(localId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadDiaryToServer() {
        TODO("Not yet implemented")
    }

    override suspend fun postDiaryToServer(eventServerId: Long, eventId: Long) {
        TODO("Not yet implemented")
    }
}