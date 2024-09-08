package com.mongmong.namo.presentation.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

object DiaryDateConverter {

    private fun parseDate(dateString: String?): Date? {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            Log.d("DiaryDateConverter", "$format")
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
            Log.d("DiaryDateConverter", "$format")
            format.format(it)
        }
    }

    @JvmStatic
    fun toDD(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("dd", Locale.getDefault())
            Log.d("DiaryDateConverter", "$format")
            format.format(it)
        }
    }

    @JvmStatic
    fun toFullDateTimeWithDay(dateString: String?): String? {
        val date = parseDate(dateString)
        return date?.let {
            val format = SimpleDateFormat("yyyy.MM.dd (EE) hh:mm", Locale.getDefault())
            Log.d("DiaryDateConverter", "$format")
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
}
