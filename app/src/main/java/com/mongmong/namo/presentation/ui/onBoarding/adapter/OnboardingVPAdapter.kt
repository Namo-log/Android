package com.mongmong.namo.presentation.ui.onBoarding.adapter

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mongmong.namo.presentation.config.BaseFragment

class OnboardingVPAdapter(
    list: ArrayList<BaseFragment<out ViewDataBinding>>,
    fm: FragmentManager,
    lifecycle: Lifecycle
        ) : FragmentStateAdapter(fm, lifecycle) {
            private val fragmentList = list

    override fun createFragment(position: Int) = fragmentList[position]

    override fun getItemCount(): Int = fragmentList.size
}