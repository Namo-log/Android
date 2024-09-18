package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.mongmong.namo.data.datasource.diary.DiaryCollectionPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryMoimPagingSource
import com.mongmong.namo.data.datasource.diary.DiaryPersonalPagingSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.data.utils.mappers.DiaryMapper.toModel
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.model.MoimDiary
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import com.mongmong.namo.domain.repositories.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val remoteDiaryDataSource: RemoteDiaryDataSource,
    private val apiService: DiaryApiService,
    private val networkChecker: NetworkChecker
) : DiaryRepository {

    /** 개인 기록 리스트 조회 **/
    override fun getPersonalDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryPersonalPagingSource(apiService, date, networkChecker)
    }

    /** 개인 기록 일정 정보 조회 **/
    override suspend fun getScheduleForDiary(scheduleId: Long): ScheduleForDiary {
        return remoteDiaryDataSource.getScheduleForDiary(scheduleId).result.toModel()
    }

    /** 개인 기록 상세 조회 **/
    override suspend fun getPersonalDiary(scheduleId: Long): DiaryDetail {
        Log.d("DiaryRepositoryImpl getDiary", "$scheduleId")
        return remoteDiaryDataSource.getPersonalDiary(scheduleId).result.toModel()
    }

    /** 개인 기록 추가 **/
    override suspend fun addPersonalDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): Boolean {
        Log.d("DiaryRepositoryImpl addDiary", "$content, $enjoyRating, $images, $scheduleId")
        return remoteDiaryDataSource.addPersonalDiary(content, enjoyRating, images, scheduleId).isSuccess
    }

    /** 개인 기록 수정 **/
    override suspend fun editPersonalDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): Boolean {
        Log.d("DiaryRepositoryImpl editDiary", "$diaryId, $content, $enjoyRating, $images, $deleteImageIds")
        return remoteDiaryDataSource.editPersonalDiary(diaryId, content, enjoyRating, images, deleteImageIds).isSuccess
    }

    /** 개인 기록 삭제 **/
    override suspend fun deletePersonalDiary(diaryId: Long): Boolean {
        Log.d("DiaryRepositoryImpl deletePersonalDiary", "$diaryId")
        return remoteDiaryDataSource.deletePersonalDiary(diaryId).isSuccess
    }

    /** 모임 기록 리스트 조회 **/
    fun getMoimDiaryPagingSource(date: String): PagingSource<Int, DiarySchedule> {
        return DiaryMoimPagingSource(apiService, date, networkChecker)
    }

    /** 모임 기록 개별 조회 **/
    override suspend fun getMoimDiary(scheduleId: Long): MoimDiaryResult {
        return remoteDiaryDataSource.getMoimDiary(scheduleId)
    }

    /** 모임 메모 개별 조회 **/
    override suspend fun getMoimMemo(scheduleId: Long): MoimDiary {
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
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    ) {
        Log.d("MoimActivity", "impl addMoimActivity")
        remoteDiaryDataSource.addMoimActivity(moimScheduleId, activityName, activityMoney, participantUserIds, createImages)
    }

    /** 모임 기록 활동 수정 **/
    override suspend fun editMoimActivity(
        activityId: Long,
        deleteImageIds: List<Long>?,
        activityName: String,
        activityMoney: Long,
        participantUserIds: List<Long>,
        createImages: List<String>?
    ) {
        Log.d("MoimActivity", "impl editMoimActivity")
        remoteDiaryDataSource.editMoimActivity(activityId, deleteImageIds, activityName, activityMoney, participantUserIds, createImages)
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


    /** v2 */
    override fun getDiaryCollectionPagingSource(
        filterType: String?,
        keyword: String?
    ): Flow<PagingData<Diary>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                DiaryCollectionPagingSource(apiService, filterType, keyword, networkChecker)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toModel() } // DTO를 도메인 모델로 변환
        }
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}