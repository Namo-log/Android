package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import androidx.paging.PagingSource
import com.mongmong.namo.data.datasource.diary.DiaryMoimPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryPersonalPagingSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.MoimDiaryResult
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.config.RoomState
import java.io.File
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val localDiaryDataSource: LocalDiaryDataSource,
    private val remoteDiaryDataSource: RemoteDiaryDataSource,
    private val diaryDao: DiaryDao,
    private val apiService: DiaryApiService,
    private val networkChecker: NetworkChecker
) : DiaryRepository {
    /** 개인 기록 개별 조회 **/
    override suspend fun getDiary(localId: Long): Diary {
        Log.d("DiaryRepositoryImpl getDiary", "$localId")
        return localDiaryDataSource.getDiary(diaryId = localId)
    }

    /** 개인 기록 추가 **/
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
                    serverId = addResponse.result.scheduleId,
                    IS_UPLOAD,
                    RoomState.DEFAULT.state
                )
            } else {
                Log.d("DiaryRepositoryImpl addDiary Fail", "$diary")
            }
        }
    }

    /** 개인 기록 수정 **/
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
                    serverId = diary.scheduleServerId,
                    IS_UPLOAD,
                    RoomState.DEFAULT.state
                )
            } else {
                // 서버 업로드 실패 시 로직
            }
        }
    }

    /** 개인 기록 삭제 **/
    override suspend fun deleteDiary(localId: Long, scheduleServerId: Long) {
        // room db에 삭제 상태로 변경
        localDiaryDataSource.updateDiaryAfterUpload(
            localId,
            scheduleServerId,
            IS_NOT_UPLOAD,
            RoomState.DELETED.state
        )
        if (networkChecker.isOnline()) {
            // 서버 db에서 삭제
            val deleteResponse = remoteDiaryDataSource.deleteDiary(scheduleServerId)
            if(deleteResponse.code == SUCCESS_CODE) {
                // room db에서 삭제
                localDiaryDataSource.deleteDiary(localId)
            } else {
                // 서버 업로드 실패 시 로직
            }
        }
    }

    /** 개인 기록 리스트 조회 **/
    override fun getPersonalDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryPersonalPagingSource(diaryDao, date)
    }
    /** 모임 기록 리스트 조회 **/
    override fun getMoimDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryMoimPagingSource(apiService, date)
    }

    /** 모임 기록 개별 조회 **/
    override suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult {
        return remoteDiaryDataSource.getDiary(scheduleId)
    }

    override suspend fun uploadDiaryToServer() {
        TODO("Not yet implemented")
    }

    override suspend fun postDiaryToServer(serverId: Long, scheduleId: Long) {
        TODO("Not yet implemented")
    }

    companion object {
        const val IS_NOT_UPLOAD = false
        const val IS_UPLOAD = true
        const val SUCCESS_CODE = 200
    }
}