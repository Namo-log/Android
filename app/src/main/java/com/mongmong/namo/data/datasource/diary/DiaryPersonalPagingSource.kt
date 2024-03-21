package com.mongmong.namo.data.datasource.diary


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.DiaryEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class DiaryPersonalPagingSource (
    private val diaryDao: DiaryDao,
    private val date: String
) : PagingSource<Int, DiaryEvent>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiaryEvent> {
        return try {
            Log.d("pagingSource load", "${params.key}")
            val page = params.key ?: 0
            val result = withContext(Dispatchers.IO) {
                diaryDao.getDiaryEventList(date, page, PAGE_SIZE).toListItems()
            }

            LoadResult.Page(
                data = result,
                prevKey = if (page == 0) null else page.minus(1),
                nextKey = if (result.isEmpty()) null else page.plus(1)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DiaryEvent>): Int? {
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

    private fun List<DiaryEvent>.toListItems(): List<DiaryEvent> {
        val result = mutableListOf<DiaryEvent>()
        var groupHeaderDate: Long = 0

        this.forEach { event ->
            if (groupHeaderDate * 1000 != event.event_start * 1000) {

                val headerEvent =
                    event.copy(event_start = event.event_start * 1000, isHeader = true)
                result.add(headerEvent)

                groupHeaderDate = event.event_start
            }
            result.add(event)
        }

        return result
    }

    companion object {
        const val PAGE_SIZE = 10
    }
}


