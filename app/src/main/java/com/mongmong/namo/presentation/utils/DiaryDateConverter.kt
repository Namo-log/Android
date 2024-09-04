package com.mongmong.namo.presentation.utils

import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale

object DiaryDateConverter {
    @JvmStatic
    fun getFormattedMonth(date: Long): String = date.let { DateTime(date * 1000).toString("MMM", Locale.ENGLISH) }
    @JvmStatic
    fun getFormattedDay(date: Long): String = date.let { DateTime(date * 1000).toString("dd") }
    @JvmStatic
    fun getFormattedDate(date: Long): String = date.let { DateTime(date * 1000).toString("yyyy.MM.dd (EE) hh:mm") }


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