package com.mongmong.namo.data.datasource.diary

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

class DiaryPersonalPagingSource(
    private val diaryDao: DiaryDao,
    private val date: String
) : PagingSource<Int, DiarySchedule>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
        return try {
            val page = params.key ?: 0
            val result = withContext(Dispatchers.IO) {
                diaryDao.getDiaryScheduleList(date, page, PAGE_SIZE)
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

    override fun getRefreshKey(state: PagingState<Int, DiarySchedule>): Int? {
        return null
    }

    // UNIX 타임스탬프를 '년-월-일' 형식의 문자열로 변환하는 함수


    companion object {
        const val PAGE_SIZE = 5
    }
}
