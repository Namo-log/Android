package com.mongmong.namo.presentation.ui.diary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.state.FilterType
import com.mongmong.namo.presentation.utils.DiaryDateConverter.toDiaryHeaderDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.text.SimpleDateFormat

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _filter = MutableLiveData<FilterType>(FilterType.NONE)
    val filter: LiveData<FilterType> = _filter

    val keyword = MutableLiveData<String>()

    private val _emptyMessageResId = MutableLiveData<Int>(R.string.diary_network_failure)
    val emptyMessageResId: LiveData<Int> = _emptyMessageResId

    private val _emptyImageResId = MutableLiveData<Int>(R.drawable.ic_network_disconnect)
    val emptyImageResId: LiveData<Int> = _emptyImageResId

    private val _isListEmpty = MutableLiveData<Boolean>(false)
    val isListEmpty: LiveData<Boolean> = _isListEmpty

    /** 일기 리스트 조회 **/
    fun getDiaryPaging(): Flow<PagingData<Diary>> {
        Log.d("getDiaryPaging", "filterType: ${_filter.value} keyword: ${keyword.value}")
        return repository.getDiaryCollectionPagingSource(filter.value?.request, keyword.value)
            .cachedIn(viewModelScope)
            .map { it.insertHeaderLogic() }
    }

    // PagingData에 날짜 구분선 헤더 추가
    private fun PagingData<Diary>.insertHeaderLogic(): PagingData<Diary> {
        return this.insertSeparators { before, after ->
            if (after == null) {
                return@insertSeparators null
            }

            val beforeDate = before?.startDate?.toDiaryHeaderDate()
            val afterDate = after.startDate.toDiaryHeaderDate()

            if (afterDate == null || (beforeDate == null || beforeDate != afterDate)) {
                // 첫 아이템이거나, 날짜가 변경될 때 헤더 아이템 추가
                after.copy(startDate = after.startDate, isHeader = true)
            } else null
        }
    }


    private fun Long.convertDate(): String {
        return SimpleDateFormat("yyyy.MM.dd").format(this * 1000)
    }

    fun setIsListEmpty(isEmpty: Boolean) { _isListEmpty.value = isEmpty }

    fun setEmptyView(messageResId: Int, imageResId: Int) {
        _isListEmpty.value = true
        _emptyMessageResId.value = messageResId
        _emptyImageResId.value = imageResId
        Log.d("setEmptyView", "${_isListEmpty.value}")
    }

    fun setFilter(filterType: FilterType) { _filter.value = filterType }
    companion object {
        const val IS_MOIM = 1
        const val IS_NOT_MOIM = 0
        const val PAGE_SIZE = 5
        const val DATE_FORMAT = "yyyy.MM"
    }
}
