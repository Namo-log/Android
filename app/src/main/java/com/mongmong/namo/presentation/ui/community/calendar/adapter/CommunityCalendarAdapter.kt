package com.mongmong.namo.presentation.ui.community.calendar.adapter

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mongmong.namo.presentation.ui.community.calendar.CommunityCalendarMonthFragment
import org.joda.time.DateTime

class CommunityCalendarAdapter(fm : FragmentActivity) : FragmentStateAdapter(fm) {

    private val _monthDayList = MutableLiveData<List<DateTime>>()
    private var start : Long = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

    override fun getItemCount(): Int = Int.MAX_VALUE

    override fun createFragment(position: Int): CommunityCalendarMonthFragment {
        var millis = getItemId(position)
        return CommunityCalendarMonthFragment.newInstance(millis)
    }

    override fun getItemId(position: Int): Long
        = DateTime(start).plusMonths(position - START_POSITION).millis

    override fun containsItem(itemId: Long): Boolean {
        val date = DateTime(itemId)

        return date.dayOfMonth == 1 && date.millisOfDay == 0
    }

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2
    }
}