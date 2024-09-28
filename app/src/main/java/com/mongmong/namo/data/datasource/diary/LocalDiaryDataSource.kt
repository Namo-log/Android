package com.mongmong.namo.data.datasource.diary

import android.util.Log
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.presentation.state.RoomState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalDiaryDataSource @Inject constructor(private val diaryDao: DiaryDao) {
    suspend fun getDiary(diaryId: Long): Diary {
        var diaryResult = Diary( // 기본 또는 오류 시 반환할 Diary 객체
            diaryId = 0L,
            scheduleServerId = 0L,
            _content = "",
            images = listOf(""),
            state = RoomState.DEFAULT.state,
            isUpload = false,
            isHeader = false
        )
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.getDiaryDaily(diaryId)
            }.onSuccess {
                Log.d("LocalDiaryDataSource getDiary Success", "$it")
                diaryResult = it
            }.onFailure {
                Log.d("LocalDiaryDataSource getDiary Fail", it.toString())
            }
        }
        return diaryResult
    }


    suspend fun addDiary(diary: Diary) {
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.insertDiary(diary)
            }.onSuccess {
                Log.d("LocalDiaryDataSource addDiary Success", "$diary")
                updateHasDiary(diary.diaryId)
            }.onFailure {
                Log.d("LocalDiaryDataSource addDiary Fail", "$diary")
            }
        }
    }

    suspend fun editDiary(diary: Diary) {
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.updateDiary(diary)
            }.onSuccess {
                Log.d("LocalDiaryDataSource editDiary Success", "$diary")
                updateHasDiary(diary.diaryId)
            }.onFailure {
                Log.d("LocalDiaryDataSource editDiary Fail", "$diary")
            }
        }
    }

    suspend fun deleteDiary(localId: Long) {
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.deleteDiary(localId)
            }.onSuccess {
                deleteHasDiary(localId)
                Log.d("LocalDiaryDataSource deleteDiary Success", "$localId")
            }.onFailure {
                Log.d("LocalDiaryDataSource deleteDiary Fail", "$localId")
            }
        }
    }


    private fun updateHasDiary(localId: Long) {
//        diaryDao.updateHasDiary(localId)
    }

    private fun deleteHasDiary(localId: Long) {
//        diaryDao.deleteHasDiary(localId)
    }

    suspend fun updateDiaryAfterUpload(
        localId: Long,
        serverId: Long,
        isUpload: Boolean,
        state: String
    ) {
        Log.d("LocalDiaryDataSource updateDiaryAfterUpload", "$localId, $serverId")
        diaryDao.updateDiaryAfterUpload(
            localId,
            serverId,
            isUpload,
            state
        )
    }

    companion object {
        const val EVENT_CURRENT_DEFAULT = "DEFAULT"
    }
}
