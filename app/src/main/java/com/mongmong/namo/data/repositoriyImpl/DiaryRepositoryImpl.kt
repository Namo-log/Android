package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import androidx.paging.PagingSource
import com.mongmong.namo.data.datasource.diary.DiaryMoimPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryPersonalPagingSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetMoimMemoResponse
import com.mongmong.namo.domain.model.GetPersonalDiaryResponse
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.config.RoomState
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val localDiaryDataSource: LocalDiaryDataSource,
    private val remoteDiaryDataSource: RemoteDiaryDataSource,
    private val diaryDao: DiaryDao,
    private val apiService: DiaryApiService,
    private val networkChecker: NetworkChecker
) : DiaryRepository {

    /** 개인 기록 리스트 조회 **/
    override fun getPersonalDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryPersonalPagingSource(apiService, date, networkChecker)
    }

    /** 개인 기록 개별 조회 **/
    override suspend fun getPersonalDiary(scheduleId: Long): GetPersonalDiaryResponse {
        Log.d("DiaryRepositoryImpl getDiary", "$scheduleId")
        return remoteDiaryDataSource.getPersonalDiary(scheduleId)
    }

    /** 개인 기록 추가 **/
    override suspend fun addPersonalDiary(
        diary: Diary,
        images: List<String>?
    ): DiaryAddResponse {
        Log.d("DiaryRepositoryImpl addDiary", "$diary")
        return remoteDiaryDataSource.addPersonalDiary(diary.images, diary.diaryId, diary.content)
        /*if (networkChecker.isOnline()) {
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
        }*/
    }

    /** 개인 기록 수정 **/
    override suspend fun editPersonalDiary(
        diary: Diary,
        images: List<String>?
    ): DiaryResponse {
        Log.d("DiaryRepositoryImpl editDiary", "$diary")
        return remoteDiaryDataSource.editPersonalDiary(diary.images, diary.diaryId, diary.content)
        /*if (networkChecker.isOnline()) {
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
        }*/
    }

    /** 개인 기록 삭제 **/
    override suspend fun deletePersonalDiary(localId: Long, scheduleServerId: Long): DiaryResponse {
        // room db에 삭제 상태로 변경
        /*localDiaryDataSource.updateDiaryAfterUpload(
            localId,
            scheduleServerId,
            IS_NOT_UPLOAD,
            RoomState.DELETED.state
        )*/
        //if (networkChecker.isOnline()) {
            // 서버 db에서 삭제
            Log.d("DiaryRepositoryImpl deletePersonalDiary", "$scheduleServerId")
            return remoteDiaryDataSource.deletePersonalDiary(scheduleServerId)
            /*if(deleteResponse.code == SUCCESS_CODE) {
                // room db에서 삭제
                localDiaryDataSource.deleteDiary(localId)
            } else {
                // 서버 업로드 실패 시 로직
            }*/
        //}
    }
    /** 모임 기록 리스트 조회 **/
    override fun getMoimDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryMoimPagingSource(apiService, date, networkChecker)
    }

    /** 모임 기록 개별 조회 **/
    override suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult {
        return remoteDiaryDataSource.getMoimDiary(scheduleId)
    }

    /** 모임 메모 개별 조회 **/
    override suspend fun getMoimMemo(scheduleId: Long): GetMoimMemoResponse {
        // 개인 기록 개별 조회 api 사용
        return remoteDiaryDataSource.getMoimMemo(scheduleId)
    }

    /** 모임 기록 메모 추가/수정 **/
    override suspend fun patchMoimMemo(scheduleId: Long, content: String): Boolean {
        return remoteDiaryDataSource.patchMoimMemo(scheduleId, content)
    }

    /** 모임 기록 메모 삭제(개인) **/
    override suspend fun deleteMoimMemo(scheduleId: Long): Boolean {
        return remoteDiaryDataSource.deleteMoimMemo(scheduleId)
    }

    /** 모임 기록 활동 추가 **/
    override suspend fun addMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    ) {
        Log.d("MoimActivity", "impl addMoimActivity")
        remoteDiaryDataSource.addMoimActivity(moimScheduleId, place, money, members, images)
    }

    /** 모임 기록 활동 수정 **/
    override suspend fun editMoimActivity(
        moimScheduleId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?
    ) {
        Log.d("MoimActivity", "impl editMoimActivity")
        remoteDiaryDataSource.editMoimActivity(moimScheduleId, place, money, members, images)
    }

    /** 모임 기록 활동 삭제**/
    override suspend fun deleteMoimActivity(activityId: Long) {
        Log.d("MoimActivity", "impl deleteMoimActivity")
        remoteDiaryDataSource.deleteMoimActivity(activityId)
    }

    /** 모임 기록 삭제 (그룹에서) **/
    override suspend fun deleteMoimDiary(moimScheduleId: Long): Boolean {
        return remoteDiaryDataSource.deleteMoimDiary(moimScheduleId)
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