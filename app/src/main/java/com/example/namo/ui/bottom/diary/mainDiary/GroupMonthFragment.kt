package com.example.namo.ui.bottom.diary.mainDiary

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.namo.databinding.FragmentDiaryGroupMonthBinding


class GroupMonthFragment(val yearMonth: String) : Fragment() {

    private var _binding: FragmentDiaryGroupMonthBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupMonthBinding.inflate(inflater, container, false)
        Log.d("yearMonthGroup", yearMonth)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}