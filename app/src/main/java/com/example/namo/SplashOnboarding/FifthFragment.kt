package com.example.namo.SplashOnboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.R
import com.example.namo.databinding.FragmentOnboarding5Binding

class FifthFragment : Fragment() {

    private var _binding : FragmentOnboarding5Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding5Binding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.onboarding5StartBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_termsFragment)
            onBoardingFinished()
        }

    }

    private fun onBoardingFinished(){
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FifthFragment()
    }
}