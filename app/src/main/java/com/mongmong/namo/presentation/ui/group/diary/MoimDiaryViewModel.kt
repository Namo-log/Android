package com.mongmong.namo.presentation.ui.group.diary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.group.MoimActivity
import com.mongmong.namo.domain.model.group.MoimDiaryResult
import com.mongmong.namo.domain.model.group.MoimScheduleBody
import com.mongmong.namo.domain.model.group.convertToGroupMembers
import com.mongmong.namo.domain.repositories.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoimDiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    private val _moimDiary = MutableLiveData<MoimDiaryResult>()
    val moimDiary: LiveData<MoimDiaryResult> = _moimDiary

    private val _activities = MutableLiveData<MutableList<MoimActivity>>(mutableListOf())
    val activities: LiveData<MutableList<MoimActivity>> = _activities

    private val _patchActivitiesComplete = MutableLiveData<Boolean>()
    val patchActivitiesComplete: LiveData<Boolean> = _patchActivitiesComplete

    private val _deleteDiaryComplete = MutableLiveData<Boolean>()
    val deleteDiaryComplete: LiveData<Boolean> = _deleteDiaryComplete

    val isEdit = MutableLiveData<Boolean>(false)
    val isParticipantVisible = MutableLiveData<Boolean>(false)
    var moimScheduleId: Long = 0L
    private val deleteItems = mutableListOf<Long>()  // 삭제할 항목 저장

    private var initialDiaryState: MoimDiaryResult? = null
    private var initialActivitiesState: List<MoimActivity> = listOf()

    /** 모임 기록 개별 조회 **/
    fun getMoimDiary(scheduleId: Long) {
        viewModelScope.launch {
            val result = repository.getMoimDiary(scheduleId)
            _moimDiary.postValue(result)
            _activities.postValue(result.moimActivities.toMutableList())
            initDiaryState(result, result.moimActivities)
        }
    }

    fun setNewMoimDiary(schedule: MoimScheduleBody) {
        val newDiary = MoimDiaryResult(
            name = schedule.name,
            startDate = schedule.startDate,
            locationName = schedule.locationName,
            users = convertToGroupMembers(schedule.users),
            moimActivities = arrayListOf(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf()))
        )
        _moimDiary.value = newDiary
        _activities.value = arrayListOf(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf()))
        initDiaryState(newDiary, arrayListOf(MoimActivity(0L, "", 0L, arrayListOf(), arrayListOf())))
    }

    fun addActivity(activity: MoimActivity) {
        _activities.value?.add(activity)
        _activities.postValue(_activities.value)
    }

    fun updateActivityName(position: Int, name: String) {
        _activities.value?.get(position)?.name = name
    }

    fun updateActivityPay(position: Int, pay: Long) {
        _activities.value?.get(position)?.pay = pay
    }

    fun updateActivityMembers(position: Int, members: List<Long>) {
        _activities.value?.get(position)?.members = members
    }

    fun updateActivityImages(position: Int, images: List<String>) {
        _activities.value?.get(position)?.imgs = images
        _activities.postValue(_activities.value)
    }

    fun updateDeleteItems(deleteItems: List<Long>) {
        this.deleteItems.clear()
        this.deleteItems.addAll(deleteItems)
    }

    fun deleteActivityImage(position: Int, image: String) {
        val updatedActivities = _activities.value?.toMutableList() ?: mutableListOf()
        val updatedImages = updatedActivities[position].imgs?.toMutableList() ?: mutableListOf()
        updatedImages.remove(image)
        updatedActivities[position].imgs = updatedImages
        _activities.postValue(updatedActivities)
    }

    fun patchMoimActivities() {
        viewModelScope.launch {
            val activities = _activities.value ?: return@launch
            activities.forEach { activity ->
                if (activity.moimActivityId == 0L) {
                    addMoimActivity(activity)
                } else {
                    editMoimActivity(activity)
                }
            }
            deleteItems.forEach { activityId ->
                deleteMoimActivity(activityId)
            }
            _patchActivitiesComplete.postValue(true)
        }
    }

    private suspend fun addMoimActivity(activity: MoimActivity) {
        val members = activity.members.ifEmpty { _moimDiary.value?.users?.map { it.userId } }
        repository.addMoimActivity(moimScheduleId, activity.name, activity.pay, members, activity.imgs)
    }

    private suspend fun editMoimActivity(activity: MoimActivity) {
        val members = activity.members.ifEmpty { _moimDiary.value?.users?.map { it.userId } }
        repository.editMoimActivity(activity.moimActivityId, activity.name, activity.pay, members, activity.imgs)
    }
    private suspend fun deleteMoimActivity(activityId: Long) {
        repository.deleteMoimActivity(activityId)
    }

    /** 모임 기록 삭제 (그룹에서) **/
    fun deleteMoimDiary() {
        viewModelScope.launch {
            _deleteDiaryComplete.postValue(repository.deleteMoimDiary(moimScheduleId))
        }
    }

    fun toggleIsParticipantVisible() {
        isParticipantVisible.value = !isParticipantVisible.value!!
    }

    // 초기 상태 저장 메서드
    private fun initDiaryState(diary: MoimDiaryResult, activities: List<MoimActivity>) {
        initialDiaryState = diary.copy()
        initialActivitiesState = activities.map { it.copy() }
    }

    // 변경 여부 확인 메서드
    fun isDiaryChanged(): Boolean {
        return _moimDiary.value != initialDiaryState || _activities.value != initialActivitiesState
    }
}
