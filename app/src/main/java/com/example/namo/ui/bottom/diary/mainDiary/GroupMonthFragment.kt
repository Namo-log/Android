package com.example.namo.ui.bottom.diary.mainDiary


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.namo.databinding.FragmentDiaryGroupMonthBinding


class GroupMonthFragment : Fragment() {

    private var _binding: FragmentDiaryGroupMonthBinding? = null
    private val binding get() = _binding!!

    var yearMonth: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            yearMonth = it.getString("yearMonth", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryGroupMonthBinding.inflate(inflater, container, false)

        Log.d("yearMonthGroup", yearMonth)

        return binding.root
    }

    companion object {
        fun newInstance(yearMonth: String) = GroupMonthFragment().apply {
            arguments = Bundle().apply {
                putString("yearMonth", yearMonth)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}