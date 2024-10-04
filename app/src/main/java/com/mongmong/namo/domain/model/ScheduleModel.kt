package com.mongmong.namo.domain.model

import org.joda.time.LocalDateTime

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
    var startDate: LocalDateTime = LocalDateTime.now(),
    var endDate: LocalDateTime = LocalDateTime.now(),
)

data class Location(
    var longitude: Double = 0.0, // 경도
    var latitude: Double = 0.0, // 위도
    var locationName: String = "없음",
    var kakaoLocationId: String? = ""
)

data class ScheduleCategoryInfo(
    var categoryId: Long = 0L,
    val colorId: Int = 0,
    val name: String = "",
)

// 캘린더에 표시되는 색상 정보 (친구: 카테고리 정보, 참석자: 색상 & 이름)
data class CalendarColorInfo(
    val colorId: Int,
    val name: String
)