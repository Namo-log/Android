package com.example.namo.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.namo.R
import com.example.namo.databinding.FragmentOnboarding3Binding

class ThirdFragment : Fragment() {

    private var _binding : FragmentOnboarding3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding3Binding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding3NextBtn.setOnClickListener {
            viewPager?.currentItem = 3
        }

        binding.onboarding3SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ThirdFragment()
    }
}