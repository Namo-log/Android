package com.gradu.presentation.ui.community

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gradu.presentation.community.friend.FriendFragment
import com.gradu.presentation.ui.community.moim.MoimFragment

class CommunityVPAdapter (fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> MoimFragment()
            else -> FriendFragment()
        }
    }
}