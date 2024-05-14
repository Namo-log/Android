package com.mongmong.namo.data.datasource.diary

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.presentation.ui.diary.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiaryPersonalPagingSource(
    private val diaryDao: DiaryDao,
    private val date: String
) : PagingSource<Int, DiarySchedule>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
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

    override fun getRefreshKey(state: PagingState<Int, DiarySchedule>): Int? {
        return null
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
    }


    companion object {
        const val PAGE_SIZE = 5
        const val LONG_DIVIDER = 1000
    }
}
