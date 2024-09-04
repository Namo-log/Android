package com.mongmong.namo.data.datasource.diary

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mongmong.namo.data.dto.GetDiaryCollectionResponse
import com.mongmong.namo.data.dto.GetDiaryCollectionResult
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.domain.model.Diary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiaryCollectionPagingSource(
    private val apiService: DiaryApiService,
    private val filterType: String?,
    private val keyword: String?,
    private val networkChecker: NetworkChecker
) : PagingSource<Int, GetDiaryCollectionResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GetDiaryCollectionResult> {
        return try {
            if(!networkChecker.isOnline()){
                Log.d("dd", "network")
                return LoadResult.Error(Exception("Network unavailable"))
            }


            val page = params.key ?: 1// 다음 페이지 번호, 초기 값은 0

            var apiResult = listOf<GetDiaryCollectionResult>()

            withContext(Dispatchers.IO) {
                runCatching {
                    apiService.getDiaryCollection(filterType, keyword, page)
                }.onSuccess { response ->
                    Log.d("DiaryCollectionPagingSource load success", "${params.key} : $response")
                    val diaries = mutableListOf<GetDiaryCollectionResult>()
                    response.result.forEach {
                        diaries.add(it)
                    }
                    apiResult = diaries
                }.onFailure {
                    Log.d("DiaryCollectionPagingSource load fail", "${params.key} : $it")
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


    override fun getRefreshKey(state: PagingState<Int, GetDiaryCollectionResult>): Int? {
        // Refresh 키 정의 (예시에서는 null 반환하여 새로고침 키 없음을 의미)
        return null
    }

    companion object {
        const val PAGE_SIZE = 5
    }
}



