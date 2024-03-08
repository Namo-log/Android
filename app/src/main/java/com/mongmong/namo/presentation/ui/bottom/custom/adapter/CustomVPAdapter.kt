package com.mongmong.namo.presentation.ui.bottom.custom.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mongmong.namo.presentation.ui.bottom.custom.CustomFontFragment
import com.mongmong.namo.presentation.ui.bottom.custom.CustomMyFragment
import com.mongmong.namo.presentation.ui.bottom.custom.CustomPaletteFragment

class CustomVPAdapter (fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CustomPaletteFragment()
            1 -> com.mongmong.namo.presentation.ui.bottom.custom.CustomFontFragment()
            else -> CustomMyFragment()
        }
    }
}