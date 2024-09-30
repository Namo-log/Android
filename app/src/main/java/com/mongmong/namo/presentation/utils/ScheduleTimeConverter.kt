package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime
import java.util.Calendar
import kotlin.math.abs

class ScheduleTimeConverter(private var clickedDate: DateTime) {
    fun getScheduleTimeText(startLong: Long, endLong: Long): String {
        val dayInterval = getDayInterval(startLong, endLong)
        if (dayInterval == 0) { // 하루 일정
            return setTimeText(
                Companion.parseTimeToFormattedText(startLong),
                Companion.parseTimeToFormattedText(endLong)
            )
        } else { // 기간 일정
            if (calculateDayIntervalWithClickedDate(startLong) == 0) { // 시작일
                return setTimeText(Companion.parseTimeToFormattedText(startLong), DAY_END)
            } else if (calculateDayIntervalWithClickedDate(endLong) == 0) { // 종료일
                return setTimeText(DAY_START, Companion.parseTimeToFormattedText(endLong))
            }
            // 중간일
            return setTimeText(DAY_START, DAY_END)
        }
    }

    // 달력에서 선택된 날짜를 받아 업데이트
    fun updateClickedDate(clickedDate: DateTime) {
        this.clickedDate = clickedDate
    }

    // 종료일 - 시작일
    private fun getDayInterval(startLong: Long, endLong: Long): Int {
        val fewDay = endLong - startLong
        return (fewDay / INTERVAL_CONVERTER).toInt()
    }

    // 기준 날짜(달력에서 선택된 날짜)와 시작일 또는 종료일의 interval 계산
    private fun calculateDayIntervalWithClickedDate(compareTime: Long): Int {
        // 기준 날짜
        val criteriaTime = parseDateTimeToMillis(clickedDate)
        val fewDay = getIgnoredTimeDays(criteriaTime) - getIgnoredTimeDays(compareTime)
        return abs((fewDay / INTERVAL_CONVERTER / LONG_CONVERTER).toInt())
    }

    // '시작 시간 - 종료 시간' 형식으로 표시
    private fun setTimeText(startDate: String, endDate: String): String {

        return "$startDate - $endDate"
    }

    // 날짜 변환 (DateTime -> Long)
    private fun parseDateTimeToMillis(compareTime: DateTime): Long {
        return compareTime.withTimeAtStartOfDay().millis / 1000L
    }

    // 시간, 분, 초, 밀리초 제외 시키기
    private fun getIgnoredTimeDays(time: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time * LONG_CONVERTER

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        const val LONG_CONVERTER = 1000L
        const val INTERVAL_CONVERTER = 24 * 60 * 60
        const val TIME_PATTERN = "HH:mm"
        const val DAY_START = "00:00"
        const val DAY_END = "23:59"

        // 시간 형식(HH:mm)으로 변환
        @JvmStatic
        fun parseTimeToFormattedText(date: Long): String {
            return DateTime(date * LONG_CONVERTER).toString(TIME_PATTERN)
        }
    }
}