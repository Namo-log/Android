package com.example.namo.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.R
import com.example.namo.databinding.FragmentLoginBinding

class LoginFragment: Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        clickHandler()

        return binding.root
    }

    private fun clickHandler(){
        binding.loginTempBt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            setLoginFinished()

        }
    }

    private fun setLoginFinished(){
        val prefs = requireActivity().getSharedPreferences("setLogin", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }
}