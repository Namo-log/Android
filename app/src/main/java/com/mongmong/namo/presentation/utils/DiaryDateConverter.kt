package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime
import java.util.Locale

object DiaryDateConverter {
    @JvmStatic
    fun getFormattedMonth(date: Long): String = date.let { DateTime(date * 1000).toString("MMM", Locale.ENGLISH) }
    @JvmStatic
    fun getFormattedDay(date: Long): String = date.let { DateTime(date * 1000).toString("dd") }
    @JvmStatic
    fun getFormattedDate(date: Long): String = date.let { DateTime(date * 1000).toString("yyyy.MM.dd (EE) HH:mm") }
}