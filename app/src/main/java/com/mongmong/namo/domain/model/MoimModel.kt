package com.mongmong.namo.domain.model

import com.mongmong.namo.domain.model.group.GroupMember
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
    var period: SchedulePeriod = SchedulePeriod(),
    var locationInfo: Location = Location(),
    val participants: List<Participant> = emptyList()
): Serializable {
    fun getParticipantsColoInfo(): List<CalendarColorInfo> {
        return participants.map { participant ->
            CalendarColorInfo(
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
    val participants: List<MoimCalendarParticipant> = emptyList(),
    val isCurMoim: Boolean = false
) {
    fun getScheduleOwnerText(): String  {
        return if (participants.size < 2) participants[0].nickname
        else participants.size.toString() + "명"
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
    val members: List<GroupMember> = emptyList()
): Serializable {
    fun getMemberNames(): String {
        return members.joinToString { it.userName }
    }

    fun getParticipantsColoInfo(): List<CalendarColorInfo> {
        return members.map { participant ->
            CalendarColorInfo(
                colorId = participant.color,
                name = participant.userName
            )
        }
    }
}