package com.mongmong.namo.presentation.ui.community.alert.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mongmong.namo.presentation.ui.community.alert.FriendAlertFragment
import com.mongmong.namo.presentation.ui.community.alert.MoimAlertFragment

class CommunityAlertVPAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity)  {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MoimAlertFragment()
            else -> FriendAlertFragment()
        }
    }
}