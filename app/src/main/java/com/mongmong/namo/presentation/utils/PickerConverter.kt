package com.mongmong.namo.presentation.utils

import org.joda.time.LocalDateTime

object PickerConverter {
    private const val DATE_FORMAT = "yyyy.MM.dd (E)"
    private const val TIME_FORMAT = "hh:mm a"
    private const val DEFAULT_START_HOUR = 8 // 오전 8시
    private const val DEFAULT_END_HOUR = 9 // 오전 9시

    fun getDefaultDate(date: LocalDateTime, isStartTime: Boolean): LocalDateTime {
        return date
            .withHourOfDay(if (isStartTime) DEFAULT_START_HOUR else DEFAULT_END_HOUR)
            .withMinuteOfHour(0)
            .withSecondOfMinute(0)
    }

    @JvmStatic
    fun getDateText(dateTime: LocalDateTime?): String? {
        if (dateTime == null) return  null
        return dateTime.toString(DATE_FORMAT)
    }

    @JvmStatic
    fun getTimeText(dateTime: LocalDateTime?): String? {
        if (dateTime == null) return  null
        return dateTime.toString(TIME_FORMAT)
    }

    fun setSelectedTime(prevDateTime: LocalDateTime, hour: Int, minute: Int): LocalDateTime {
        return prevDateTime
            .withHourOfDay(hour)
            .withMinuteOfHour(minute)
            .withSecondOfMinute(0)
    }
}