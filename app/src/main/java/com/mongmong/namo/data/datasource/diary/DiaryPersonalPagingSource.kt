package com.mongmong.namo.data.datasource.diary

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.model.DiarySchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiaryPersonalPagingSource(
    private val apiService: DiaryApiService,
    private val date: String,
    private val networkChecker: NetworkChecker
) : PagingSource<Int, DiarySchedule>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
        return try {
            if(!networkChecker.isOnline()){
                Log.d("dd", "network")
                return LoadResult.Error(Exception("Network unavailable"))
            }


            val page = params.key ?: 0// 다음 페이지 번호, 초기 값은 0

            var diaryItems = listOf<DiarySchedule>()

            withContext(Dispatchers.IO) {
                runCatching {
                    apiService.getPersonalMonthDiary(date, page, DiaryMoimPagingSource.PAGE_SIZE)
                }.onSuccess { response ->
                    Log.d("MoimPagingSource load success", "${params.key} : $response")
                    val diarySchedules = mutableListOf<DiarySchedule>()
                    response.result.content.forEach {
                        diarySchedules.add(
                            DiarySchedule(
                                it.scheduleId,
                                it.title,
                                it.startDate,
                                it.categoryId,
                                it.placeName,
                                it._content,
                                it.images,
                                color = it.color
                            )
                        )
                    }
                    diaryItems = diarySchedules
                }.onFailure {
                    Log.d("MoimPagingSource load fail", "${params.key} : $it")
                }

            }


            LoadResult.Page(
                data = diaryItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (diaryItems.size < PAGE_SIZE) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DiarySchedule>): Int? {
        return null
    }

    /*override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
        return try {
            val page = params.key ?: 0
            val result = withContext(Dispatchers.IO) {
                diaryDao.getDiaryScheduleList(getMonthStartDateInMillis(date) / LONG_DIVIDER, getNextMonthStartDateInMillis(date) / LONG_DIVIDER, page, PAGE_SIZE)
            }
            Log.d("personalPagingSource load", "${params.key} : $result")
            LoadResult.Page(
                data = result,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (result.size < PAGE_SIZE) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // 피커 날짜를 Long 타입으로 변환
    private fun getMonthStartDateInMillis(yearMonth: String): Long {
        val dateFormat = SimpleDateFormat(DiaryViewModel.DATE_FORMAT, Locale.getDefault())
        val startDate = dateFormat.parse(yearMonth)

        val calendar = Calendar.getInstance()
        calendar.time = startDate!!
        // 시작일을 0시 0분 0초로 설정
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    private fun getNextMonthStartDateInMillis(yearMonth: String): Long {
        val startDateInMillis = getMonthStartDateInMillis(yearMonth)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDateInMillis
        // 다음 달로 이동
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }*/


    companion object {
        const val PAGE_SIZE = 5
        const val LONG_DIVIDER = 1000
    }
}
