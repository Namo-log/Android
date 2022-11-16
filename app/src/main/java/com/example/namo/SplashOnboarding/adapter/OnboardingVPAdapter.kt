package com.example.namo.SplashOnboarding.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class OnboardingVPAdapter (
    list : ArrayList<Fragment>,
    fm : FragmentManager,
    lifecycle: Lifecycle
        ) : FragmentStateAdapter(fm, lifecycle) {
            private val fragmentList = list

    override fun createFragment(position: Int) = fragmentList[position]

    override fun getItemCount(): Int = fragmentList.size
}