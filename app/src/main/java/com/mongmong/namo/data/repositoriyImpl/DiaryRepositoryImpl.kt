package com.mongmong.namo.data.repositoriyImpl

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mongmong.namo.data.datasource.diary.DiaryCollectionPagingSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.model.CalendarDiaryDate
import com.mongmong.namo.data.utils.mappers.DiaryMapper.toModel
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.model.DiaryBaseResponse
import com.mongmong.namo.domain.model.DiaryDetail
import com.mongmong.namo.domain.model.MoimPayment
import com.mongmong.namo.domain.model.ScheduleForDiary
import com.mongmong.namo.domain.repositories.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val remoteDiaryDataSource: RemoteDiaryDataSource,
    private val apiService: DiaryApiService,
    private val networkChecker: NetworkChecker
) : DiaryRepository {

    /** 기록 */
    // 기록 보관함 리스트 조회
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

    //기록 일정 정보 조회
    override suspend fun getScheduleForDiary(scheduleId: Long): ScheduleForDiary {
        return remoteDiaryDataSource.getScheduleForDiary(scheduleId).result.toModel()
    }

    // 기록 상세 조회
    override suspend fun getDiary(scheduleId: Long): DiaryDetail {
        Log.d("DiaryRepositoryImpl getDiary", "$scheduleId")
        return remoteDiaryDataSource.getDiary(scheduleId).result.toModel()
    }

    // 기록 추가
    override suspend fun addDiary(
        content: String,
        enjoyRating: Int,
        images: List<String>,
        scheduleId: Long
    ): DiaryBaseResponse {
        Log.d("DiaryRepositoryImpl addDiary", "$content, $enjoyRating, $images, $scheduleId")
        return remoteDiaryDataSource.addPersonalDiary(content, enjoyRating, images, scheduleId)
    }

    // 기록 수정
    override suspend fun editDiary(
        diaryId: Long,
        content: String,
        enjoyRating: Int,
        images: List<String>,
        deleteImageIds: List<Long>
    ): DiaryBaseResponse {
        Log.d("DiaryRepositoryImpl editDiary", "$diaryId, $content, $enjoyRating, $images, $deleteImageIds")
        return remoteDiaryDataSource.editPersonalDiary(diaryId, content, enjoyRating, images, deleteImageIds)
    }

    // 기록 삭제
    override suspend fun deleteDiary(diaryId: Long): DiaryBaseResponse {
        Log.d("DiaryRepositoryImpl deletePersonalDiary", "$diaryId")
        return remoteDiaryDataSource.deletePersonalDiary(diaryId)
    }

    // 기록 캘린더 조회
    override suspend fun getCalendarDiary(yearMonth: String): CalendarDiaryDate {
        return remoteDiaryDataSource.getCalendarDiary(yearMonth).result.toModel()
    }

    // 날짜별 기록 조회 (기록 캘린더)
    override suspend fun getDiaryByDate(date: String): List<Diary> {
        return remoteDiaryDataSource.getDiaryByDate(date).result.map { it.toModel() }
    }

    override suspend fun getMoimPayment(scheduleId: Long): MoimPayment {
        return remoteDiaryDataSource.getMoimPayment(scheduleId).result.toModel()
    }


    companion object {
        const val PAGE_SIZE = 5
    }
}