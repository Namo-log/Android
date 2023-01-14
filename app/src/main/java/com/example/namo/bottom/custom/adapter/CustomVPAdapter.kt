package com.example.namo.bottom.custom.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.namo.bottom.custom.CustomFontFragment
import com.example.namo.bottom.custom.CustomMyFragment
import com.example.namo.bottom.custom.CustomPaletteFragment

class CustomVPAdapter (fragment: Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CustomPaletteFragment()
            1 -> CustomFontFragment()
            else -> CustomMyFragment()
        }
    }
}