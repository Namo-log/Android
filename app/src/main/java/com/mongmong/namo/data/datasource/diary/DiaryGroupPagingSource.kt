package com.mongmong.namo.data.datasource.diary


import android.view.View
import android.widget.TextView
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import com.mongmong.namo.data.remote.diary.DiaryService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DiaryGroupPagingSource(
    private val month: String,
    private val recyclerView: RecyclerView,
    private val textView: TextView
) : PagingSource<Int, DiarySchedule>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DiarySchedule> {
        return try {
            val nextPageNumber = params.key ?: 0 // 다음 페이지 번호, 초기 값은 0
            val diarySchedules = arrayListOf<DiarySchedule>()
            val service = DiaryService()

            val response = service.getGroupMonthDiary(month, nextPageNumber, 10)
            val result = response.result.content
            result.forEach {
                diarySchedules.add(
                    DiarySchedule(
                        it.scheduleIdx,
                        it.title,
                        it.startDate,
                        it.categoryId,
                        it.placeName,
                        it.content,
                        it.imgUrl
                    )
                )
            }
            val diaryItems = diarySchedules.toListItems()

            if (nextPageNumber == 0 && result.isEmpty()) {    // 달 별 메모 없으면 없다고 띄우기
                withContext(Dispatchers.Main) {
                    val context = recyclerView.context
                    recyclerView.visibility = View.GONE
                    textView.visibility = View.VISIBLE
                    textView.text = context.resources.getString(R.string.diary_empty)
                }
            }

            LoadResult.Page(
                data = diaryItems,
                prevKey = if (nextPageNumber == 0) null else nextPageNumber - 1,
                nextKey = if (diaryItems.isEmpty()) null else nextPageNumber + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, DiarySchedule>): Int? {
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

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
