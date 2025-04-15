package com.gradu.data.utils.mappers

import com.gradu.core.utils.converter.ScheduleDateConverter
import com.gradu.data.dto.GetMonthScheduleResult
import com.gradu.data.dto.Period
import com.gradu.data.dto.ScheduleLocation
import com.gradu.data.dto.ScheduleRequestBody
import com.gradu.domain.model.Location
import com.gradu.domain.model.Schedule
import com.gradu.domain.model.ScheduleCategoryInfo
import com.gradu.domain.model.SchedulePeriod
import com.gradu.domain.model.ScheduleType

object ScheduleMapper {
    // DTO -> Model
    fun GetMonthScheduleResult.toModel(): Schedule {
        return Schedule(
            scheduleId = this.scheduleId,
            title = this.title,
            period = SchedulePeriod( // LocalDateTime 형태로 변환
                ScheduleDateConverter.parseServerDateToLocalDateTime(this.startDate),
                ScheduleDateConverter.parseServerDateToLocalDateTime(this.endDate),
            ),
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
            isMeetingSchedule = (this.scheduleType == ScheduleType.MOIM.value)
        )
    }

    // Model -> DTO
    fun Schedule.toDTO(): ScheduleRequestBody {
        return ScheduleRequestBody(
            title = this.title,
            categoryId = this.categoryInfo.categoryId,
            period = Period(
                this.period.startDate.toString(),
                this.period.endDate.toString()
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