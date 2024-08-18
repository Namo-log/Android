package com.mongmong.namo.presentation.utils

import com.mongmong.namo.domain.model.group.GroupMember

object StringConverter {
    @JvmStatic
    fun getMembersText(memberList: List<GroupMember>): String {
        if (memberList.isEmpty()) return "없음"
        return memberList.joinToString(", ") { it.userName }
    }
}