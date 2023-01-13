package com.example.namo.bottom.home.calendar

import org.joda.time.DateTime

interface DayItemClickListener {
    fun onSendDate(date : DateTime)
}