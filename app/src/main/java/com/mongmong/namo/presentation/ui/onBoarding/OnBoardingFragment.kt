package com.mongmong.namo.presentation.ui.onBoarding

import android.content.Context
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboardingBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.onBoarding.adapter.OnboardingVPAdapter
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnBoardingFragment : BaseFragment<FragmentOnboardingBinding>(R.layout.fragment_onboarding) {
    private lateinit var dotsIndicator : DotsIndicator

    override fun setup() {
        dotsIndicator = binding.onboardingDotsIndicatorDi

        navigateToLogin()
        setupViewPager()
    }

    private fun setupViewPager(){
        val fragmentList = arrayListOf(
            OnBoardingFirstFragment.newInstance(),
            OnBoardingSecondFragment.newInstance(),
            OnBoardingThirdFragment.newInstance(),
            OnBoardingFourthFragment.newInstance(),
            OnBoardingFifthFragment.newInstance()
        )

        val adapter = OnboardingVPAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        binding.onboardingViewPagerVp.adapter = adapter
        dotsIndicator.attachTo(binding.onboardingViewPagerVp)

        binding.onboardingViewPagerVp.isUserInputEnabled = false //스와이프 이동 막기
    }

    private fun isOnBoardingFinished() : Boolean {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    private fun navigateToLogin() {
        if (isOnBoardingFinished()) {
            findNavController().navigate(R.id.action_onBoardingFragment_to_loginFragment)
        }
    }
}