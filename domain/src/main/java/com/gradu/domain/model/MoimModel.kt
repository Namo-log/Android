package com.gradu.domain.model

import org.joda.time.LocalDateTime
import java.io.Serializable

data class MoimPreview(
    val moimId: Long = 0L,
    var startDate: LocalDateTime = LocalDateTime.now(),
    var coverImg: String = "",
    var title: String = "",
    var participantCount: Int = 0,
    var participantNicknames: String = ""
): Serializable

data class MoimScheduleDetail(
    val moimId: Long = 0L,
    var title: String = "",
    var coverImg: String = "",
    var period: com.gradu.domain.model.SchedulePeriod = com.gradu.domain.model.SchedulePeriod(),
    var locationInfo: com.gradu.domain.model.Location = com.gradu.domain.model.Location(),
    val participants: List<com.gradu.domain.model.Participant> = emptyList()
): Serializable {
    fun getParticipantsColoInfo(): List<com.gradu.domain.model.CalendarColorInfo> {
        return participants.map { participant ->
            com.gradu.domain.model.CalendarColorInfo(
                colorId = participant.colorId,
                name = participant.nickname
            )
        }
    }
}

data class Participant(
    val participantId: Long = 0L,
    val userId: Long = 0L,
    val isGuest: Boolean = false,
    val nickname: String = "",
    val colorId: Int = 0,
    val isOwner: Boolean = false
): Serializable

data class MoimCalendarSchedule(
    val scheduleId: Long = 0L,
    val title: String = "",
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime = LocalDateTime.now(),
    val participants: List<com.gradu.domain.model.MoimCalendarParticipant> = emptyList(),
    val isCurMoim: Boolean = false
) {
    fun convertToCommunityModel(): com.gradu.domain.model.CommunityCommonSchedule {
        return com.gradu.domain.model.CommunityCommonSchedule(
            scheduleId = this.scheduleId,
            title = this.title,
            startDate = this.startDate,
            endDate = this.endDate,
            participants = this.participants,
            categoryInfo = null,
            type = if (isCurMoim) com.gradu.domain.model.ScheduleType.MOIM else com.gradu.domain.model.ScheduleType.PERSONAL
        )
    }
}

data class MoimCalendarParticipant(
    val participantId: Long = 0L,
    val userId: Long = 0L,
    val nickname: String = "",
    val colorId: Int = 0,
)

data class Moim(
    val moimId: Long = 0L,
    var startDate: LocalDateTime = LocalDateTime.now(),
    var coverImg: String = "",
    var title: String = "",
    var placeName: String = "",
    val members: List<com.gradu.domain.model.Participant> = emptyList()
): Serializable {
    fun getMemberNames(): String {
        return members.joinToString { it.nickname }
    }

    fun getParticipantsColoInfo(): List<com.gradu.domain.model.CalendarColorInfo> {
        return members.map { participant ->
            com.gradu.domain.model.CalendarColorInfo(
                colorId = participant.colorId,
                name = participant.nickname
            )
        }
    }
}

data class MoimCreateInfo(
    val moimId: Long = 0L,
    val title: String = ""
): Serializable