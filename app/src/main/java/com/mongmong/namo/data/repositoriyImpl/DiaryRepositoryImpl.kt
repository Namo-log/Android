package com.mongmong.namo.data.repositoriyImpl

import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.RemoteDiaryDataSource
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.domain.repositories.DiaryRepository
import java.io.File
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val localDiaryDataSource: LocalDiaryDataSource,
    private val remoteDiaryDataSource: RemoteDiaryDataSource,
    private val networkChecker: NetworkChecker
) : DiaryRepository {
    override suspend fun getDiary(localId: Long) {
        remoteDiaryDataSource
    }

    override suspend fun addDiary(
        diary: Diary,
        diaryLocalId: Long,
        content: String,
        images: List<File>?,
        serverId: Long,
    ) {
        localDiaryDataSource.addDiary(diary)
        if(networkChecker.isOnline()){
            val diaryAddResponse = remoteDiaryDataSource.addDiaryToServer(content, images, serverId)
            if(diaryAddResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.updateDiaryAfterUpload(localId = diaryLocalId, response = diaryAddResponse)
            } else {
                // 서버 업로드 실패 시 로직
            }
        }
    }

    override suspend fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun uploadDiaryToServer() {
        TODO("Not yet implemented")
    }

    override suspend fun postDiaryToServer(eventServerId: Long, eventId: Long) {
        TODO("Not yet implemented")
    }

    companion object {
        const val SUCCESS_CODE = 200
    }
}