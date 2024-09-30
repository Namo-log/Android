package com.mongmong.namo.data.dto

/** 모임 일정 목록 조회 */
data class GetMoimResponse(
    val result: List<GetMoimResult>
)

data class GetMoimResult(
    val meetingScheduleId: Long = 0,
    val title: String = "",
    val startDate: Long = 0L,
    val imageUrl: String = "",
    val participantCount: Int = 0,
    val participantNicknames: String = ""
)