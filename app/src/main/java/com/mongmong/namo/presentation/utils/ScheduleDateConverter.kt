package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime

object ScheduleDateConverter {
    private const val SCHEDULE_CLICKED_DATE_FORMAT = "MM.dd (E)"
    private const val SERVER_DATE_FORMAT = "yyyy-MM-dd"

    @JvmStatic
    fun parseDateTimeToClickedDateText(date: DateTime?): String? { // 클릭한 날짜
        return date?.toString(SCHEDULE_CLICKED_DATE_FORMAT)
    }

    fun parseDateTimeToServerData(date: DateTime): String {
        return date.toString(SERVER_DATE_FORMAT)
    }
}