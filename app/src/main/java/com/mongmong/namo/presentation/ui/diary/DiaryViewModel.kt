package com.mongmong.namo.presentation.ui.diary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.google.android.material.tabs.TabLayout
import com.mongmong.namo.R
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

    private val _currentDate = MutableLiveData<String>(DateTime().toString(DATE_FORMAT))
    val currentDate: LiveData<String> = _currentDate

    private val _isMoim = MutableLiveData<Int>(0)
    val isMoim: LiveData<Int> = _isMoim

    private val _emptyMessageResId = MutableLiveData<Int>(R.string.diary_network_failure)
    val emptyMessageResId: LiveData<Int> = _emptyMessageResId

    private val _emptyImageResId = MutableLiveData<Int>(R.drawable.ic_network_disconnect)
    val emptyImageResId: LiveData<Int> = _emptyImageResId

    private val _isListEmpty = MutableLiveData<Boolean>(false)
    val isListEmpty: LiveData<Boolean> = _isListEmpty

    /** 개인 기록 리스트 조회 **/
    fun getPersonalPaging(date: String): Flow<PagingData<DiarySchedule>> {
        Log.d("getPersonalPaging", "date: $date")
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
        Log.d("getMoimPaging", "date: $date")
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
            if (after == null) {
                return@insertSeparators null
            }
            if (before == null || before.startDate.convertDate() != after.startDate.convertDate()) {
                // 첫 아이템, 날짜가 변경될 때 헤더 아이템 추가
                after.copy(startDate = after.startDate * 1000, isHeader = true)
            } else {
                // 같은 날짜 내의 아이템 처리
                null
            }
        }
    }

    private fun Long.convertDate(): String {
        return SimpleDateFormat("yyyy.MM.dd").format(this * 1000)
    }

    /** 선택한 피커 날짜 **/
    fun setCurrentDate(date: String) {
        _currentDate.value = date
    }
    fun getFormattedDate() = currentDate.value!!.split(".").let { "${it[0]},${it[1].removePrefix("0")}" }

    /** 개인/그룹 여부 토글 **/
    fun getIsMoim(): Int = _isMoim.value ?: 0
    fun setIsMoim(isMoim: Boolean) {
        _isMoim.value = if (isMoim) IS_MOIM else IS_NOT_MOIM
    }

    fun setIsListEmpty(isEmpty: Boolean) { _isListEmpty.value = isEmpty }
    fun setEmptyView(messageResId: Int, imageResId: Int) {
        _isListEmpty.value = true
        _emptyMessageResId.value = messageResId
        _emptyImageResId.value = imageResId
        Log.d("setEmptyView", "${_isListEmpty.value}")
    }

    val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            setIsMoim(tab.position == IS_MOIM)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {}
        override fun onTabReselected(tab: TabLayout.Tab) {}
    }

    companion object {
        const val IS_MOIM = 1
        const val IS_NOT_MOIM = 0
        const val PAGE_SIZE = 5
        const val DATE_FORMAT = "yyyy.MM"
    }
}
