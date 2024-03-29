package com.mongmong.namo.presentation.ui.bottom.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.domain.repositories.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import org.joda.time.DateTime
import java.text.SimpleDateFormat

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _currentDate = MutableLiveData<String>(DateTime().toString("yyyy.MM"))
    val currentDate : LiveData<String> = _currentDate

    private val _isMoim = MutableLiveData<Int>(0)
    val isMoim : LiveData<Int> = _isMoim



    /** 개인 기록 리스트 조회 **/
    fun getPersonalPaging(date: String): Flow<PagingData<DiarySchedule>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { repository.getPersonalDiaryPagingSource(date) }
        ).flow.cachedIn(viewModelScope).map { pagingData -> pagingData.insertHeaderLogic() }
    }

    /** 모임 기록 리스트 조회 **/
    fun getMoimPaging(date: String): Flow<PagingData<DiarySchedule>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { repository.getMoimDiaryPagingSource(date) }
        ).flow.cachedIn(viewModelScope).map { pagingData -> pagingData.insertHeaderLogic() }
    }

    // PagingData에 날짜 구분선 헤더 추가
    private fun PagingData<DiarySchedule>.insertHeaderLogic(): PagingData<DiarySchedule> {
        return this.insertSeparators { before, after ->
            if (after == null) { return@insertSeparators null }
            if (before == null || before.startDate.convertDate() != after.startDate.convertDate()) {
                // 첫 아이템, 날짜가 변경될 때 헤더 아이템 추가
                after.copy(startDate = after.startDate * 1000, isHeader = true)
            } else {
                // 같은 날짜 내의 아이템 처리
                null
            }
        }
    }
    private fun Long.convertDate() : String {
        return SimpleDateFormat("yyyy.MM.dd").format(this * 1000)
    }

    /** 선택 날짜 **/
    fun getCurrentDate(): String = _currentDate.value ?: DateTime().toString("yyyy.MM")
    fun setCurrentDate(yearMonth: String) { _currentDate.value = yearMonth }
    /** 개인/그룹 여부 토글  **/
    fun getIsGroup(): Int = _isMoim.value ?: 0
    fun setIsGroup(isGroup: Boolean) { _isMoim.value = if(isGroup) IS_GROUP else IS_NOT_GROUP }
    companion object {
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
        const val PAGE_SIZE = 5
    }
}