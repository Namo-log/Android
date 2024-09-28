package com.mongmong.namo.data.utils.mappers

import com.mongmong.namo.data.dto.GetMonthScheduleResult
import com.mongmong.namo.data.dto.Period
import com.mongmong.namo.data.dto.ScheduleLocation
import com.mongmong.namo.data.dto.ScheduleRequestBody
import com.mongmong.namo.domain.model.Location
import com.mongmong.namo.domain.model.Schedule
import com.mongmong.namo.domain.model.ScheduleCategoryInfo

object ScheduleMapper {
    // DTO -> Model
    fun GetMonthScheduleResult.toModel(): Schedule {
        return Schedule(
            scheduleId = this.scheduleId,
            title = this.title,
            startLong = this.startDate,
            endLong = this.endDate,
            locationInfo = Location(
                this.locationInfo.longitude,
                this.locationInfo.latitude,
                this.locationInfo.locationName,
                this.locationInfo.kakaoLocationId
            ),
            categoryInfo = ScheduleCategoryInfo(
                this.categoryInfo.categoryId,
                this.categoryInfo.colorId,
                this.categoryInfo.name
            ),
            alarmList = this.alarmDate,
            hasDiary = this.hasDiary,
            isMeetingSchedule = this.isMeetingSchedule
        )
    }

    // Model -> DTO
    fun Schedule.toModel(): ScheduleRequestBody {
        return ScheduleRequestBody(
            title = this.title,
            categoryId = this.categoryInfo.categoryId,
            period = Period(
                this.startLong,
                this.endLong
            ),
            location = ScheduleLocation(
                this.locationInfo.longitude,
                this.locationInfo.latitude,
                this.locationInfo.locationName,
                this.locationInfo.kakaoLocationId
            ),
            reminderTrigger = this.alarmList
        )
    }
}