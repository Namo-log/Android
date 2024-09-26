package com.mongmong.namo.domain.model

import com.mongmong.namo.data.dto.Period
import com.mongmong.namo.data.dto.ScheduleCategoryInfo
import com.mongmong.namo.data.dto.ScheduleLocation
import com.mongmong.namo.data.dto.ScheduleRequestBody
import java.io.Serializable

data class Schedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var startLong: Long = 0,
    var endLong: Long = 0,
    var locationInfo: ScheduleLocation = ScheduleLocation(),
    var categoryInfo: ScheduleCategoryInfo = ScheduleCategoryInfo(),
    var alarmList: List<Int>? = listOf(),
    var hasDiary: Boolean? = false,
    var isMeetingSchedule: Boolean = false
) : Serializable {
    fun convertLocalScheduleToServer() : ScheduleRequestBody {
        return ScheduleRequestBody(
            title = this.title,
            categoryId = this.categoryInfo.categoryId,
            period = Period(this.startLong, this.endLong),
            location = this.locationInfo,
            reminderTrigger = this.alarmList,
        )
    }
}

// 캘린더에 표시되는 색상 정보 (친구: 카테고리 정보, 참석자: 색상 & 이름)
data class CalendarColorInfo(
    val colorId: Int,
    val name: String
)