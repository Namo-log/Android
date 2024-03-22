package com.mongmong.namo.presentation.ui.bottom.diary.mainDiary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.diary.DiarySchedule
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.presentation.config.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import org.joda.time.DateTime
import org.joda.time.YearMonth

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _diary = MutableLiveData<Diary>()
    val diary: LiveData<Diary> = _diary

    private val _imgList = MutableLiveData<List<String>>(emptyList())
    val imgList: LiveData<List<String>> = _imgList

    private val _currentDate = MutableLiveData<String>(DateTime().toString("yyyy.MM"))
    val currentDate : LiveData<String> = _currentDate

    private val _isGroup = MutableLiveData<Int>(0)
    val isGroup : LiveData<Int> = _isGroup

    /** 개인 기록 리스트 조회 **/
    fun getPersonalPaging(date: String): Flow<PagingData<DiarySchedule>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { repository.getPersonalDiaryPagingSource(date) }
        ).flow.cachedIn(viewModelScope)
    }
    /** 개인 기록 개별 조회 **/
    fun getExistingDiary(diaryId: Long) {
        viewModelScope.launch {
            Log.d("DiaryViewModel getDiary", "$diaryId")
            _diary.postValue(repository.getDiary(diaryId))
        }
    }
    /** 개인 기록 추가시 데이터 초기화 **/
    fun setNewDiary(event: Schedule, content: String) {
        _diary.value = Diary(
            diaryId = event.scheduleId,
            serverId = event.serverId,
            content = content,
            images = _imgList.value,
            state = RoomState.ADDED.state
        )
    }

    /** 개인 기록 추가 **/
    fun addDiary(images: List<File>?) {
        viewModelScope.launch {
            Log.d("DiaryViewModel addDiary", "$diary")
            _diary.value?.let {
                repository.addDiary(
                    diary = it,
                    images = images
                )
            }
        }
    }
    /** 개인 기록 수정 **/
    fun editDiary(content: String, images: List<File>?) {
        viewModelScope.launch {
            _diary.value?.let {
                it.content = content
                it.state = RoomState.EDITED.state
            }
            Log.d("DiaryViewModel editDiary", "${_diary.value}")
            _diary.value?.let {
                repository.editDiary(
                    diary = it,
                    images = images
                )
            }
        }
    }
    /** 개인 기록 삭제 **/
    fun deleteDiary(localId: Long, scheduleServerId: Long) {
        viewModelScope.launch {
            repository.deleteDiary(localId, scheduleServerId)
        }
    }

    fun getImgList() = _imgList.value
    fun updateImgList(newImgList: List<String>) {
        _imgList.value = newImgList
        _diary.value?.images = _imgList.value
    }

    /** 선택 날짜 **/
    fun getCurrentDate(): String = _currentDate.value ?: DateTime().toString("yyyy.MM")
    fun setCurrentDate(yearMonth: String) { _currentDate.value = yearMonth }
    /** 개인/그룹 여부 토글  **/
    fun getIsGroup(): Int = _isGroup.value ?: 0
    fun setIsGroup(isGroup: Boolean) { _isGroup.value = if(isGroup) IS_GROUP else IS_NOT_GROUP }
    companion object {
        const val EVENT_CURRENT_ADDED = "ADDED"
        const val EVENT_CURRENT_EDITED = "EDITED"
        const val IS_GROUP = 1
        const val IS_NOT_GROUP = 0
        const val PAGE_SIZE = 10
    }
}