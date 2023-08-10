package com.example.namo.ui.bottom.diary.mainDiary.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.namo.ui.bottom.diary.mainDiary.GroupMonthFragment
import com.example.namo.ui.bottom.diary.mainDiary.PersonalMonthFragment

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val yearMonth: String
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 2 // 전체 탭의 수
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PersonalMonthFragment(yearMonth)
            1 -> GroupMonthFragment(yearMonth)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}

