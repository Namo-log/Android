package com.mongmong.namo.presentation.ui.group.calendar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.joda.time.DateTime

class GroupCalendarAdapter(fm : FragmentActivity) : FragmentStateAdapter(fm) {

    private var start : Long = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): GroupCalendarMonthFragment {
        var millis = getItemId(position)
        return GroupCalendarMonthFragment.newInstance(millis)
    }

    override fun getItemId(position: Int): Long
        = DateTime(start).plusMonths(position - START_POSITION).millis

    override fun containsItem(itemId: Long): Boolean {
        val date = DateTime(itemId)

        return date.dayOfMonth == 1 && date.millisOfDay == 0
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2

        var GROUP_ID : Long = 0L
    }
}