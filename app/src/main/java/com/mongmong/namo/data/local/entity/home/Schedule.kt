package com.mongmong.namo.data.local.entity.home

import com.mongmong.namo.domain.model.Location
import com.mongmong.namo.domain.model.Period
import com.mongmong.namo.domain.model.ScheduleCategoryInfo
import com.mongmong.namo.domain.model.ScheduleRequestBody
import java.io.Serializable

data class Schedule(
    var scheduleId: Long = 0L,
    var title: String = "",
    var startLong: Long = 0,
    var endLong: Long = 0,
    var locationInfo: Location = Location(),
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