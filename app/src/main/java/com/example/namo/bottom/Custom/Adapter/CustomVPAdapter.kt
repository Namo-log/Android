package com.example.namo.bottom.Custom.Adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.namo.bottom.Custom.CustomFontFragment
import com.example.namo.bottom.Custom.CustomMyFragment
import com.example.namo.bottom.Custom.CustomPaletteFragment

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