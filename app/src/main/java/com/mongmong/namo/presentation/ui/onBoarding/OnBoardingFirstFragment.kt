package com.mongmong.namo.presentation.ui.onBoarding

import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding1Binding
import com.mongmong.namo.presentation.config.BaseFragment

class OnBoardingFirstFragment : BaseFragment<FragmentOnboarding1Binding>(R.layout.fragment_onboarding_1) {

    override fun setup() {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding1NextBtn.setOnClickListener {
            viewPager?.currentItem = 1
        }

        binding.onboarding1SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }

    companion object {
        fun newInstance() = OnBoardingFirstFragment()
    }
}