package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
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
    override suspend fun getDiary(localId: Long): Diary {
        Log.d("DiaryRepositoryImpl addDiary", "$localId")
        return localDiaryDataSource.getDiary(diaryId = localId)
    }

    override suspend fun addDiary(
        diary: Diary,
        images: List<File>?
    ) {
        Log.d("DiaryRepositoryImpl addDiary", "$diary")
        localDiaryDataSource.addDiary(diary)
        if(networkChecker.isOnline()){
            val diaryAddResponse = remoteDiaryDataSource.addDiaryToServer(diary, images)
            if(diaryAddResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.updateDiaryAfterUpload(localId = diary.diaryId, serverId = diaryAddResponse.result.scheduleIdx)
            } else {
                Log.d("DiaryRepositoryImpl addDiary Fail", "$diary")
            }
        }
    }

    override suspend fun editDiary(
        diary: Diary,
        images: List<File>?
    ) {
        Log.d("DiaryRepositoryImpl editDiary", "$diary")
        localDiaryDataSource.editDiary(diary)
        if(networkChecker.isOnline()){
            val diaryResponse = remoteDiaryDataSource.editDiaryToServer(diary, images)
            if(diaryResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.updateDiaryAfterUpload(localId = diary.diaryId, serverId = diary.serverId)
            } else {
                // 서버 업로드 실패 시 로직
            }
        }
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