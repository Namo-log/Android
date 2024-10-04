package com.mongmong.namo.presentation.ui.community.moim

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mongmong.namo.domain.model.Moim
import com.mongmong.namo.domain.model.group.GroupMember
import org.joda.time.LocalDateTime

class MoimViewModel: ViewModel() {
    private val _moimList = MutableLiveData<List<Moim>>(emptyList())
    val moimList: LiveData<List<Moim>> = _moimList

    // 임시 데이터
    var moim = Moim(1, LocalDateTime.now(), "https://img.freepik.com/free-photo/beautiful-floral-composition_23-2150968962.jpg", "나모 모임 일정", "강남역",
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