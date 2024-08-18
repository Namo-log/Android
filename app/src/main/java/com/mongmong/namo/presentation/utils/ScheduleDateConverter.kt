package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime

object ScheduleDateConverter {
    private const val SCHEDULE_CLICKED_DATE_FORMAT = "MM.dd (E)"

    @JvmStatic
    fun parseDateTimeToClickedDateText(date: DateTime?): String? { // 클릭한 날짜
        return date?.toString(SCHEDULE_CLICKED_DATE_FORMAT)
    }
}