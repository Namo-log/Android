package com.example.namo.bottom.home.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.namo.databinding.FragmentScheduleDialogBasicBinding

class ScheduleDialogBasicFragment : Fragment() {

    private lateinit var binding : FragmentScheduleDialogBasicBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentScheduleDialogBasicBinding.inflate(inflater, container, false)

        return binding.root
    }
}