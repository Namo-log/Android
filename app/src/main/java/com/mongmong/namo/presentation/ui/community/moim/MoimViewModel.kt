package com.mongmong.namo.presentation.ui.community.moim

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.presentation.utils.PickerConverter
import org.joda.time.DateTime

class MoimViewModel: ViewModel() {
    private val _moimList = MutableLiveData<List<Moim>>(emptyList())
    val moimList: LiveData<List<Moim>> = _moimList

    // 임시 데이터
    var moim = Moim(1, PickerConverter.parseDateTimeToLong(DateTime.now()), "https://github.com/nahy-512/nahy-512/assets/101113025/3fb8e968-e482-4aff-9334-60c41014a80f", "나모 모임 일정", "강남역",
        listOf(
            GroupMember(3, "코코아", 4),
            GroupMember(2, "짱구", 6),
        )
    )

    init {
        _moimList.value = listOf(
            moim
        )
    }
}