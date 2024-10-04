package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.threeten.bp.ZoneId

object ScheduleDateConverter {
    private const val SCHEDULE_CLICKED_DATE_FORMAT = "MM.dd (E)"
    private const val SERVER_FILTERING_DATE_FORMAT = "yyyy-MM-dd"
    private const val FULL_DATE_FORMAT = "yyyy.MM.dd (EE) HH:mm"

    @JvmStatic
    fun parseDateTimeToClickedDateText(date: DateTime?): String? { // 클릭한 날짜
        return date?.toString(SCHEDULE_CLICKED_DATE_FORMAT)
    }

    @JvmStatic
    fun getFullDateText(date: LocalDateTime?): String? {
        return date?.toString(FULL_DATE_FORMAT)
    }

    fun parseDateTimeToServerData(date: DateTime): String {
        return date.toString(SERVER_FILTERING_DATE_FORMAT)
    }

    @JvmStatic
    fun parseServerDateToLocalDateTime(serverDate: String): LocalDateTime { // "2024-08-28T14:11:52" 형태의 서버 데이터
        // 서버로부터 받은 날짜를 LocalDateTime으로 파싱
        return LocalDateTime.parse(serverDate)
    }

    //TODO: 캘린더에서 DateTime 대신 LocalDateTime을 바로 쓸 수 있는지 확인 필요
    fun parseLocalDateTimeToDateTime(date: LocalDateTime): DateTime {
        return DateTime(date
            .toDateTime(DateTimeZone.forID("Asia/Seoul")))
            .withTimeAtStartOfDay()
    }
}