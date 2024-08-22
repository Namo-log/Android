package com.mongmong.namo.presentation.ui.community.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mongmong.namo.presentation.ui.community.friend.FriendFragment
import com.mongmong.namo.presentation.ui.community.moim.MoimFragment
import com.mongmong.namo.presentation.ui.custom.CustomMyFragment

class CommunityVPAdapter (fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MoimFragment()
            else -> FriendFragment()
        }
    }
}