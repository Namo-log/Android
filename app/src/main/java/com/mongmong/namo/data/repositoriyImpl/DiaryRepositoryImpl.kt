package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import com.mongmong.namo.R
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
        if (networkChecker.isOnline()) {
            val addResponse = remoteDiaryDataSource.addDiaryToServer(diary, images)
            if (addResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.updateDiaryAfterUpload(
                    localId = diary.diaryId,
                    serverId = addResponse.result.scheduleIdx,
                    IS_UPLOAD,
                    R.string.event_current_default.toString()
                )
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
        if (networkChecker.isOnline()) {
            val editResponse = remoteDiaryDataSource.editDiaryToServer(diary, images)
            if (editResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.updateDiaryAfterUpload(
                    localId = diary.diaryId,
                    serverId = diary.serverId,
                    IS_UPLOAD,
                    R.string.event_current_default.toString()
                )
            } else {
                // 서버 업로드 실패 시 로직
            }
        }
    }

    override suspend fun deleteDiary(localId: Long, scheduleServerId: Long) {
        localDiaryDataSource.updateDiaryAfterUpload(
            localId,
            scheduleServerId,
            IS_NOT_UPLOAD,
            R.string.event_current_deleted.toString()
        )
        if (networkChecker.isOnline()) {
            val deleteResponse = remoteDiaryDataSource.deleteDiary(scheduleServerId)
            if(deleteResponse.code == SUCCESS_CODE) {
                localDiaryDataSource.deleteDiary(localId)
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
        const val EVENT_CURRENT_DELETED = "DELETED"
        const val EVENT_CURRENT_DEFAULT = "DEFAULT"
        const val IS_NOT_UPLOAD = 0
        const val IS_UPLOAD = 1
        const val SUCCESS_CODE = 200
    }
}