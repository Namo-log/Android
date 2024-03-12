package com.mongmong.namo.data.remote.diary

import com.mongmong.namo.domain.model.DiaryAddResponse
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetMonthResponse
import com.mongmong.namo.domain.model.DiaryResponse
import com.mongmong.namo.domain.model.GetGroupDiaryResponse

interface AddPersonalDiaryView {
    fun onAddDiarySuccess(response: DiaryAddResponse, localId: Long)
    fun onAddDiaryFailure(message: String)
}

interface DiaryDetailView {
    fun onEditDiarySuccess(response: DiaryResponse, localId: Long, serverId: Long)
    fun onEditDiaryFailure(message: String)
    fun onDeleteDiarySuccess(response: DiaryResponse, localId: Long)
    fun onDeleteDiaryFailure(message: String)
}

interface GetMonthDiaryView {
    fun onGetMonthDiarySuccess(response: DiaryGetAllResponse)
    fun onGetMonthDiaryFailure(message: String)
}

interface GetGroupDiaryView {
    fun onGetGroupDiarySuccess(response: GetGroupDiaryResponse)
    fun onGetGroupDiaryFailure(message: String)
}


interface GetGroupMonthView {
    fun onGetGroupMonthSuccess(response: DiaryGetMonthResponse)
    fun onGetGroupMonthFailure(message: String)
}

interface AddGroupAfterDiaryView{
    fun onAddGroupAfterDiarySuccess(response: DiaryResponse)
    fun onAddGroupAfterDiaryFailure(message: String)
}

interface DiaryBasicView{
    fun onSuccess(response: DiaryResponse)
    fun onFailure(message: String)
}