package com.mongmong.namo.data.datasource

import android.util.Log
import com.mongmong.namo.R
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.model.DiaryAddResponse
import javax.inject.Inject

class LocalDiaryDataSource @Inject constructor(private val diaryDao: DiaryDao) {
    suspend fun addDiary(diary: Diary) {
        Log.d("LocalDiaryDataSource addDiary", "$diary")
        diaryDao.insertDiary(diary)
    }

    suspend fun updateDiaryAfterUpload(localId: Long, response: DiaryAddResponse) {
        Log.d("LocalDiaryDataSource updateDiaryAfterUpload", "$localId, $response")
        diaryDao.updateDiaryAfterUpload(
            localId,
            response.result.scheduleIdx,
            UPLOAD_SUCCESS,
            EVENT_CURRENT_DEFAULT
        )
    }

    companion object {
        const val UPLOAD_SUCCESS = 1
        const val EVENT_CURRENT_DEFAULT = "DEFAULT"
    }
}
