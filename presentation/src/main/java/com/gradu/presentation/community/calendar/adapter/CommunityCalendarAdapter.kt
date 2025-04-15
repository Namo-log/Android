package com.gradu.presentation.community.calendar.adapter

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gradu.presentation.community.calendar.CommunityCalendarMonthFragment
import kotlinx.datetime.*

class CommunityCalendarAdapter(fm : FragmentActivity) : FragmentStateAdapter(fm) {

    private val _monthDayList = MutableLiveData<List<LocalDateTime>>()
    private var start: LocalDateTime = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .run { LocalDateTime(year, monthNumber, 1, 0, 0) }

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): CommunityCalendarMonthFragment {
        var millis = getItemId(position)
        return CommunityCalendarMonthFragment.newInstance(millis)
    }

    override fun getItemId(position: Int): Long {
        val timeZone = TimeZone.currentSystemDefault()
        val month = start
            .toInstant(timeZone)
            .plus(DateTimePeriod(months = position - START_POSITION), timeZone)
            .toLocalDateTime(timeZone)

        return month.toInstant(timeZone).toEpochMilliseconds()
    }




    override fun containsItem(itemId: Long): Boolean {
        val date = Instant.fromEpochMilliseconds(itemId)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        return date.dayOfMonth == 1 && date.hour == 0 && date.minute == 0
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}