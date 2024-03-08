package com.mongmong.namo.presentation.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding4Binding

class FourthFragment : Fragment() {

    private var _binding : FragmentOnboarding4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding4Binding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.onboarding_viewPager_vp)

        binding.onboarding4NextBtn.setOnClickListener {
            viewPager?.currentItem = 4
        }

        binding.onboarding4SkipLayout.setOnClickListener {
            viewPager?.currentItem = 4
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FourthFragment()
    }
}