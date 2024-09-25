package com.mongmong.namo.data.utils.mappers

import com.mongmong.namo.data.dto.GetMonthScheduleResult
import com.mongmong.namo.domain.model.Schedule

object ScheduleMapper {
    fun GetMonthScheduleResult.toModel(): Schedule {
        return Schedule(
            scheduleId = this.scheduleId,
            title = this.title,
            startLong = this.startDate,
            endLong = this.endDate,
            locationInfo = this.locationInfo,
            categoryInfo = this.categoryInfo,
            alarmList = this.alarmDate,
            hasDiary = this.hasDiary,
            isMeetingSchedule = this.isMeetingSchedule
        )
    }
}