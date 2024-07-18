package com.mongmong.namo.presentation.utils

import com.mongmong.namo.domain.model.group.GroupMember

object StringConverter {
    @JvmStatic
    fun getMembersText(memberList: List<GroupMember>): String {
        return memberList.joinToString(", ") { it.userName }
    }
}