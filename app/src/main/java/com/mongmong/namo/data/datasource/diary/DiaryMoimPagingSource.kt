package com.mongmong.namo.data.datasource.diary


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DiaryMoimPagingSource(
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
                    apiService.getGroupMonthDiary(date, page, PAGE_SIZE)
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
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}



