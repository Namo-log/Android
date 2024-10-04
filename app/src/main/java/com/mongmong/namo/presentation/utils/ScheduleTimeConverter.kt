package com.mongmong.namo.presentation.utils

import android.util.Log
import com.mongmong.namo.domain.model.SchedulePeriod
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDateTime

class ScheduleTimeConverter(private var clickedDate: DateTime) {
    fun getScheduleTimeText(period: SchedulePeriod): String {
        val dayInterval = getDayInterval(period.startDate, period.endDate)
        if (dayInterval == 0) { // 하루 일정
            return setTimeText(period.startDate, period.endDate)
        } else { // 기간 일정
            if (calculateDayIntervalWithClickedDate(period.startDate) == 0) { // 시작일
                Log.e("ScheduleTimeConverter", "calculateDayIntervalWithClickedDate - startDate")
                return setTimeText(period.startDate, null)
            } else if (calculateDayIntervalWithClickedDate(period.endDate) == 0) { // 종료일
                Log.e("ScheduleTimeConverter", "calculateDayIntervalWithClickedDate - endDate")
                return setTimeText(null, period.endDate)
            }
            // 중간일
            return setTimeText(null, null)
        }
    }

    // 달력에서 선택된 날짜를 받아 업데이트
    fun updateClickedDate(clickedDate: DateTime) {
        this.clickedDate = clickedDate
    }

    // 종료일 - 시작일
    private fun getDayInterval(startDate: LocalDateTime, endDate: LocalDateTime): Int {
        return Days.daysBetween(startDate, endDate).days
    }

    // 기준 날짜(달력에서 선택된 날짜)와 시작일 또는 종료일의 interval 계산
    private fun calculateDayIntervalWithClickedDate(compareDate: LocalDateTime): Int {
        Log.d("ScheduleTimeConverter", "dayInterval: ${Days.daysBetween(clickedDate.toLocalDateTime(), compareDate).days}")
        Log.d("ScheduleTimeConverter", "clickedDate: $clickedDate\ncompareDate: $compareDate")

        return Days.daysBetween(
            clickedDate.toLocalDateTime(),
            compareDate
                .withHourOfDay(0)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
        ).days
    }

    // '시작 시간 - 종료 시간' 형식으로 표시 (ex. 11:00 - 23:59)
    private fun setTimeText(startDate: LocalDateTime?, endDate: LocalDateTime?): String {
        val startText = startDate?.let {
            parseTimeToFormattedText(it)
        } ?: DAY_START
        val endText = endDate?.let {
            parseTimeToFormattedText(it)
        } ?: DAY_END
        return "$startText - $endText"
    }

    // 시간 형식(HH:mm)으로 변환
    private fun parseTimeToFormattedText(date: LocalDateTime): String {
        return date.toString(TIME_PATTERN)
    }

    companion object {
        const val TIME_PATTERN = "HH:mm"
        const val DAY_START = "00:00"
        const val DAY_END = "23:59"
    }
}