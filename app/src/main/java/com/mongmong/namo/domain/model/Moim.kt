package com.mongmong.namo.domain.model

import com.mongmong.namo.domain.model.group.GroupMember

data class Moim(
    val startDate: Long,
    val coverImg: String,
    val title: String,
    val members: List<GroupMember> = emptyList()
) {
    fun getMemberNames(): String {
        return members.joinToString { it.userName }
    }
}