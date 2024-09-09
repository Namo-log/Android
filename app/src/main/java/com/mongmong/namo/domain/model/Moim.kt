package com.mongmong.namo.domain.model

import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.presentation.utils.PickerConverter
import org.joda.time.DateTime
import java.io.Serializable

//TODO: 종료일, 장소 좌표 등 데이터 추가
data class Moim(
    val moimId: Long = 0L,
    var startDate: Long = PickerConverter.parseDateTimeToLong(DateTime.now()),
    var coverImg: String = "",
    var title: String = "",
    var placeName: String = "",
    val members: List<GroupMember> = emptyList()
): Serializable {
    fun getMemberNames(): String {
        return members.joinToString { it.userName }
    }
}