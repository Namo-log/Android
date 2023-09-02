package com.example.namo.ui.bottom.diary.mainDiary.adapter



import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.namo.ui.bottom.diary.mainDiary.GroupMonthFragment
import com.example.namo.ui.bottom.diary.mainDiary.PersonalMonthFragment



class ViewPagerAdapter(val yearMonth: String, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // 전체 탭의 수
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PersonalMonthFragment.newInstance(yearMonth)
            1 -> GroupMonthFragment.newInstance(yearMonth)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}

