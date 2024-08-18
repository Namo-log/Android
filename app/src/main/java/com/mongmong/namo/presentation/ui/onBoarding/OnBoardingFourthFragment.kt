package com.mongmong.namo.presentation.ui.onBoarding

import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding4Binding
import com.mongmong.namo.presentation.config.BaseFragment

class OnBoardingFourthFragment : BaseFragment<FragmentOnboarding4Binding>(R.layout.fragment_onboarding_4) {

    override fun setup() {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding4NextBtn.setOnClickListener {
            viewPager?.currentItem = 4
        }

        binding.onboarding4SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }
    companion object {
        fun newInstance() = OnBoardingFourthFragment()
    }
}