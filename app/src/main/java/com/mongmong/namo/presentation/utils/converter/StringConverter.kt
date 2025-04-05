package com.mongmong.namo.presentation.utils.converter

import com.gradu.domain.model.Participant

object StringConverter {
    @JvmStatic
    fun getMembersText(memberList: List<com.gradu.domain.model.Participant>): String {
        if (memberList.isEmpty()) return "없음"
        return memberList.joinToString(", ") { it.nickname }
    }
}