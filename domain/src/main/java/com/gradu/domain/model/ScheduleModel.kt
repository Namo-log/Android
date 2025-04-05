package com.gradu.domain.model

import com.mongmong.namo.presentation.utils.converter.PickerConverter.getDefaultDate
import org.joda.time.LocalDateTime
import java.io.Serializable

data class Schedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var period: SchedulePeriod = SchedulePeriod(),
    var locationInfo: Location = Location(),
    var categoryInfo: ScheduleCategoryInfo = ScheduleCategoryInfo(),
    var alarmList: List<Int>? = listOf(),
    var hasDiary: Boolean? = false,
    var isMeetingSchedule: Boolean = false
)

data class SchedulePeriod(
    var startDate: LocalDateTime = getDefaultDate(LocalDateTime.now(), true),
    var endDate: LocalDateTime = getDefaultDate(LocalDateTime.now(), false),
): Serializable

data class Location(
    var longitude: Double = 0.0, // 경도
    var latitude: Double = 0.0, // 위도
    var locationName: String = "없음",
    var kakaoLocationId: String? = ""
): Serializable

data class ScheduleCategoryInfo(
    var categoryId: Long = 0L,
    val colorId: Int = 0,
    val name: String = "",
)

// 캘린더에 표시되는 색상 정보 (친구: 카테고리 정보, 참석자: 색상 & 이름)
data class CalendarColorInfo(
    val colorId: Int,
    val name: String
) {
    fun getCategoryColorInfo(): CalendarColorInfo {
        return CalendarColorInfo(this.colorId, this.name)
    }
}

data class CommunityCommonSchedule(
    val scheduleId: Long = 0L,
    val title: String = "",
    val startDate: LocalDateTime = LocalDateTime.now(),
    val endDate: LocalDateTime = LocalDateTime.now(),
    val participants: List<com.gradu.domain.model.MoimCalendarParticipant>? = emptyList(),
    val categoryInfo: ScheduleCategoryInfo?,
    val type: ScheduleType
) {
    fun getScheduleOwnerText(): String  {
        return if (participants!!.size < 2) participants[0].nickname
        else participants.size.toString() + "명"
    }

    fun convertToSchedule(): Schedule {
        return Schedule(
            scheduleId = this.scheduleId,
            title = this.title,
            period = SchedulePeriod(
                this.startDate,
                this.endDate
            ),
            categoryInfo = this.categoryInfo!!
        )
    }
}

enum class ScheduleType(val value: Int) {
    PERSONAL(0),
    MOIM(1),
    BIRTHDAY(2)
}