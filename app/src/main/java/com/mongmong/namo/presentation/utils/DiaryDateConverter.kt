package com.mongmong.namo.presentation.utils

import android.util.Log
import com.mongmong.namo.domain.model.CalendarDay
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

object DiaryDateConverter {

    private fun parseDate(dateString: String?): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            format.parse(dateString)
        } catch (e: Exception) {
            null
        }
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


    fun formatDateToDiaryString(year: Int, monthOfYear: Int, dayOfMonth: Int, time: String): String {
        // year, month, day, time을 조합해 "yyyy-MM-dd'T'HH:mm:ss" 형식의 String 반환
        return "$year-${String.format("%02d", monthOfYear + 1)}-${String.format("%02d", dayOfMonth)}T$time"
    }

    fun formatTimeToDiaryString(hour: Int, minute: Int, seconds: String): String {
        // 시간과 분을 받아 "HH:mm:ss" 형식으로 변환
        return "${String.format("%02d", hour)}:${String.format("%02d", minute)}:$seconds"
    }
}
