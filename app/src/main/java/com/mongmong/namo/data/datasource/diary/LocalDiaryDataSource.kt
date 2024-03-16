package com.mongmong.namo.data.datasource.diary

import android.util.Log
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.model.DiaryAddResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LocalDiaryDataSource @Inject constructor(private val diaryDao: DiaryDao) {
    suspend fun getDiary(diaryId: Long): Diary = withContext(Dispatchers.IO) {
        runCatching {
            diaryDao.getDiaryDaily(diaryId)
        }.getOrElse {
            Log.d("LocalDiaryDataSource getDiary Fail", it.toString())
            Diary( // 기본 또는 오류 시 반환할 Diary 객체
                diaryId = 0L,
                serverId = 0L,
                content = "",
                images = listOf(""),
                state = EVENT_CURRENT_DEFAULT,
                isUpload = 0,
                isHeader = false
            )
        }
    }


    suspend fun addDiary(diary: Diary) {
        Log.d("LocalDiaryDataSource addDiary", "$diary")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.insertDiary(diary)
            }.onSuccess {
                updateHasDiary(diary.diaryId)
            }.onFailure {
                Log.d("LocalDiaryDataSource addDiary Fail", "$diary")
            }
        }
    }

    suspend fun editDiary(diary: Diary) {
        Log.d("LocalDiaryDataSource editDiary", "$diary")
        withContext(Dispatchers.IO) {
            runCatching {
                diaryDao.updateDiary(diary)
            }.onSuccess {
                updateHasDiary(diary.diaryId)
            }.onFailure {
                Log.d("LocalDiaryDataSource editDiary Fail", "$diary")
            }
        }
    }

    private fun updateHasDiary(localId: Long) {
        diaryDao.updateHasDiary(localId)
    }

    suspend fun updateDiaryAfterUpload(localId: Long, serverId: Long) {
        Log.d("LocalDiaryDataSource updateDiaryAfterUpload", "$localId, $serverId")
        diaryDao.updateDiaryAfterUpload(
            localId,
            serverId,
            UPLOAD_SUCCESS,
            EVENT_CURRENT_DEFAULT
        )
    }


    companion object {
        const val UPLOAD_SUCCESS = 1
        const val EVENT_CURRENT_DEFAULT = "DEFAULT"
    }
}
