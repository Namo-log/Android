package com.mongmong.namo.presentation.ui.onBoarding

import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding2Binding
import com.mongmong.namo.presentation.config.BaseFragment

class OnBoardingSecondFragment : BaseFragment<FragmentOnboarding2Binding>(R.layout.fragment_onboarding_2) {

    override fun setup() {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding2NextBtn.setOnClickListener {
            viewPager?.currentItem = 2
        }

        binding.onboarding2SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }

    companion object {
        fun newInstance() = OnBoardingSecondFragment()
    }
}