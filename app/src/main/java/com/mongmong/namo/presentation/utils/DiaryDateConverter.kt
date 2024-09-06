package com.mongmong.namo.presentation.utils

import com.mongmong.namo.presentation.utils.DiaryDateConverter.toDiaryHeaderDate
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale

object DiaryDateConverter {
    @JvmStatic
    fun getFormattedMonth(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val date = inputFormat.parse(date)

        return outputFormat.format(date)
    }

    @JvmStatic
    fun getFormattedDay(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd", Locale.getDefault())

        val date = inputFormat.parse(date)

        return outputFormat.format(date)
    }

    @JvmStatic
    fun getFormattedDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd (EE) hh:mm", Locale.getDefault())

        val date = inputFormat.parse(date)

        return outputFormat.format(date)
    }

    @JvmStatic
    fun getFormattedTime(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm", Locale.getDefault())

        val date = inputFormat.parse(date)

        return outputFormat.format(date)
    }

    fun String.toDiaryHeaderDate(): String {
        // 입력 형식과 출력 형식을 정의합니다.
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

        // 문자열을 Date 객체로 파싱
        val date = inputFormat.parse(this)

        // 파싱한 Date 객체를 원하는 형식으로 변환하여 반환
        return outputFormat.format(date)
    }
}