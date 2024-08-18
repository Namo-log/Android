package com.mongmong.namo.presentation.ui.onBoarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding3Binding
import com.mongmong.namo.presentation.config.BaseFragment

class OnBoardingThirdFragment : BaseFragment<FragmentOnboarding3Binding>(R.layout.fragment_onboarding_3) {

    override fun setup() {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding3NextBtn.setOnClickListener {
            viewPager?.currentItem = 3
        }

        binding.onboarding3SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }


    companion object {
        fun newInstance() = OnBoardingThirdFragment()
    }
}