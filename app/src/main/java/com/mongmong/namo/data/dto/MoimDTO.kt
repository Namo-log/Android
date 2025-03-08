package com.mongmong.namo.data.dto

import com.mongmong.namo.domain.model.BaseResponse

data class MoimBaseResponse(
    val result: String = ""
): BaseResponse()

/** 모임 일정 목록 조회 */
data class GetMoimResponse(
    val result: List<GetMoimResult>
)

data class GetMoimResult(
    val meetingScheduleId: Long = 0,
    val title: String = "",
    val startDate: String = "",
    val imageUrl: String = "",
    val participantCount: Int = 0,
    val participantNicknames: String = ""
)

/** 모임 일정 상세 조회 */
data class GetMoimDetailResponse(
    val result: GetMoimDetailResult
)

data class GetMoimDetailResult(
    val scheduleId: Long = 0,
    val title: String = "",
    val imageUrl: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val locationInfo: ScheduleLocation = ScheduleLocation(),
    val participants: List<MoimParticipant> = emptyList()
)

data class MoimParticipant(
    val participantId: Long = 0L,
    val userId: Long = 0L,
    val isGuest: Boolean = false,
    val nickname: String = "",
    val colorId: Int = 0,
    val isOwner: Boolean = false
)

/** 모임 캘린더 조회 */
data class GetMoimCalendarResponse(
    val result: ArrayList<GetMoimCalendarResult>
)

data class GetMoimCalendarResult(
    val scheduleId: Long = 0L,
    val title: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val participants: List<CalendarParticipant> = emptyList(),
    val isCurMeetingSchedule: Boolean = false
)

data class CalendarParticipant(
    val participantId: Long = 0L,
    val userId: Long = 0L,
    val nickname: String = "",
    val colorId: Int = 0,
)

/** 모임 일정 생성 */
data class MoimScheduleRequestBody(
    var title: String = "",
    var imageUrl: String = "",
    var period: Period = Period(),
    var location: ScheduleLocation = ScheduleLocation(),
    var participants: List<Long> = listOf(),
)

data class PostMoimScheduleResponse(
    val result : Long
) : BaseResponse()

/** 모임 일정 수정 */
data class EditMoimScheduleRequestBody(
    var title: String = "",
    var imageUrl: String = "",
    var period: Period = Period(),
    var location: ScheduleLocation = ScheduleLocation(),
    var participantsToAdd: List<Long> = listOf(),
    var participantsToRemove: List<Long> = listOf()
)

/** 모임 일정 프로필 변경 */
data class EditMoimScheduleProfileRequestBody(
    var title: String = "",
    var imageUrl: String = ""
)

/** 모임 일정 참석자 초대 */
data class InviteMoimParticipantRequestBody(
    var memberIds: List<Long>
)