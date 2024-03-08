package com.mongmong.namo.presentation.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mongmong.namo.databinding.FragmentOnboardingBinding
import com.mongmong.namo.presentation.ui.splash.adapter.OnboardingVPAdapter
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class OnBoardingFragment : Fragment() {

    private var _binding : FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var dotsIndicator : DotsIndicator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        dotsIndicator = binding.onboardingDotsIndicatorDi

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
    }

    private fun setupViewPager(){
        val fragmentList = arrayListOf(
            FirstFragment.newInstance(),
            SecondFragment.newInstance(),
            ThirdFragment.newInstance(),
            FourthFragment.newInstance(),
            FifthFragment.newInstance()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}