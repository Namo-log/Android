package com.gradu.core.utils.converter

import com.mongmong.namo.domain.model.CalendarDay
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

object DiaryDateConverter {

    fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        val formats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (formatStr in formats) {
            try {
                val format = SimpleDateFormat(formatStr, Locale.getDefault())
                return format.parse(dateString)
            } catch (e: Exception) {
                // 파싱 실패 시 다음 형식으로 시도
            }
        }
        return null // 모든 형식에서 파싱 실패 시 null 반환
    }


    @JvmStatic
    fun toMMM(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("MMM", Locale.getDefault())
            format.format(it)
        }
    }

    @JvmStatic
    fun toDD(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("dd", Locale.getDefault())
            format.format(it)
        }
    }

    @JvmStatic
    fun toFullDateTimeWithDay(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("yyyy.MM.dd (EE) hh:mm", Locale.getDefault())
            format.format(it)
        }
    }

    @JvmStatic
    fun toDate(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("yyyy.MM.dd (EE)", Locale.getDefault())
            format.format(it)
        }
    }

    @JvmStatic
    fun to12HourTime(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            format.format(it)
        }
    }

    @JvmStatic
    fun toTime(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("hh:mm", Locale.getDefault())
            format.format(it)
        }
    }


    fun String.toDiaryHeaderDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        val date = inputFormat.parse(this) ?: return ""  // null 처리
        return outputFormat.format(date)
    }

    @JvmStatic
    fun getFormattedDate(date: Long): String = date.let { DateTime(date * 1000).toString("yyyy.MM.dd (EE) HH:mm") }

    fun CalendarDay.toYearMonth(): String = "${this.year}-${String.format("%02d", this.month + 1)}"

    fun formatDateTime(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }

}
