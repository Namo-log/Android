package com.example.namo.bottom.Custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.namo.databinding.FragmentCustomBinding


class CustomFragment : Fragment() {

    lateinit var binding: FragmentCustomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCustomBinding.inflate(inflater, container, false)

        return binding.root
    }
}