package com.mongmong.namo.data.datasource.diary


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DiaryPersonalPagingSource (
    private val diaryDao: DiaryDao,
    private val date: String
) : PagingSource<Int, DiarySchedule>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
        return try {

            val page = params.key ?: 0
            val result = withContext(Dispatchers.IO) {
                diaryDao.getDiaryScheduleList(date, page, PAGE_SIZE).toListItems()
            }
            Log.d("pagingSource load", "${params.key} : $result")
            LoadResult.Page(
                data = result,
                prevKey = if (page == 0) null else page.minus(1),
                nextKey = if (result.isEmpty()) null else page.plus(1)
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DiarySchedule>): Int? {
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

    private fun List<DiarySchedule>.toListItems(): List<DiarySchedule> {
        val result = mutableListOf<DiarySchedule>()
        var groupHeaderDate: Long = 0

        this.forEach { event ->
            if (groupHeaderDate * 1000 != event.startDate * 1000) {

                val headerSchedule =
                    event.copy(startDate = event.startDate * 1000, isHeader = true)
                result.add(headerSchedule)

                groupHeaderDate = event.startDate
            }
            result.add(event)
        }

        return result
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}


