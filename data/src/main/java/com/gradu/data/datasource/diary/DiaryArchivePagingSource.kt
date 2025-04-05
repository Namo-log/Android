package com.gradu.data.datasource.diary

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.dto.GetDiaryArchiveResult
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiaryArchivePagingSource(
    private val apiService: DiaryApiService,
    private val filterType: String?,
    private val keyword: String?,
    private val networkChecker: NetworkChecker
) : PagingSource<Int, GetDiaryArchiveResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GetDiaryArchiveResult> {
        return try {
            if(!networkChecker.isOnline()){
                Log.d("dd", "network")
                return LoadResult.Error(Exception("Network unavailable"))
            }


            val page = params.key ?: 1// 다음 페이지 번호, 초기 값은 0

            var apiResult = listOf<GetDiaryArchiveResult>()

            withContext(Dispatchers.IO) {
                runCatching {
                    apiService.getDiaryArchive(filterType, keyword, page)
                }.onSuccess { response ->
                    Log.d("DiaryArchivePagingSource load success", "${params.key} : $response")
                    val diaries = mutableListOf<GetDiaryArchiveResult>()
                    response.result.forEach {
                        diaries.add(it)
                    }
                    apiResult = diaries
                }.onFailure {
                    Log.d("DiaryArchivePagingSource load fail", "${params.key} : $it")
                }

            }


            LoadResult.Page(
                data = apiResult,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (apiResult.size < PAGE_SIZE) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, GetDiaryArchiveResult>): Int? {
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}



