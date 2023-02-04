package com.example.namo.ui.bottom.home.calendar

import org.joda.time.DateTime

interface DayItemClickListener {
    fun onSendDate(date : DateTime)
}