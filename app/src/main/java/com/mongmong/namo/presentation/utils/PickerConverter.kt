package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime

object PickerConverter {
    private const val DATE_FORMAT = "yyyy.MM.dd (E)"
    private const val TIME_FORMAT = "hh:mm a"
    private const val DEFAULT_START_HOUR = 8 // 오전 8시
    private const val DEFAULT_END_HOUR = 9 // 오전 9시

    fun getDefaultDate(date: DateTime, isStartTime: Boolean): DateTime {
        return DateTime(date.year, date.monthOfYear, date.dayOfMonth, if (isStartTime) DEFAULT_START_HOUR else DEFAULT_END_HOUR, 0, 0, 0)
    }

    fun parseDateTimeToDateText(dateTime: DateTime): String {
        return dateTime.toString(DATE_FORMAT)
    }

    fun parseDateTimeToTimeText(dateTime: DateTime): String {
        return dateTime.toString(TIME_FORMAT)
    }

    fun parseLongToDateTime(longDate: Long): DateTime {
        return DateTime(longDate * 1000L)
    }

    fun setSelectedDate(year: Int, monthOfYear: Int, dayOfMonth: Int, dateTime: DateTime): DateTime {
        return DateTime(year, monthOfYear + 1, dayOfMonth, dateTime.hourOfDay, dateTime.minuteOfHour)
    }

    fun setSelectedTime(prevDateTime: DateTime, hour: Int, minute: Int): DateTime {
        return prevDateTime.withTime(hour, minute, 0, 0)
    }
}