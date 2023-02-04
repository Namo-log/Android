package com.example.namo.ui.bottom.diary

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.databinding.FragmentDiaryBinding
import org.joda.time.LocalDateTime
import java.util.*


class DiaryFragment: Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private var datetime= LocalDateTime()
    private var diaryData = ArrayList<Diary>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        dummy()

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        binding.diaryMonth.text=LocalDateTime.now().toString("yyyy.MM")
        initRecyclerview()
    }

    private fun dialogCreate() {

        YearMonthDialog(datetime){
            binding.diaryMonth.text=LocalDateTime(it).toString("yyyy.MM")
        }.show(parentFragmentManager,"test")
    }


    private fun initRecyclerview() {

        binding.diaryListRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val listAdapter = DiaryListRVAdapter(requireContext(), diaryData){
            position->onItemClick(position)
        }
        binding.diaryListRv.adapter = listAdapter
    }

    private fun onItemClick(position:Int){
        val act=DiaryFragmentDirections.actionDiaryFragmentToDiaryDetailFragment(position)
        findNavController().navigate(act)
    }


    private fun dummy() {

        diaryData.apply {
            add(
                Diary(
                    "#DE8989",
                    LocalDateTime(2023,1,4,1,22,22),
                    "더미 1",
                    "nnnnnnnnnnnnnnnnnn",
                    mutableListOf(Gallery(R.drawable.bg_gradient_splash))
                )
            )
            add(
                Diary(
                    "#E1B000",
                    LocalDateTime(2023,1,25,1,12,22),
                    "더미 2",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        Gallery(R.drawable.ic_bottom_custom_no_select)
                    )
                )
            )
            add(
                Diary(
                    "#5C8596",
                    LocalDateTime(2023,1,8,1,0,22),
                    "더미 3",
                    "nnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        Gallery(R.drawable.ic_bottom_diary_no_select),
                        Gallery(R.drawable.ic_bottom_diary_no_select),
                        Gallery(R.drawable.ic_bottom_custom_select)
                    )
                )
            )
            add(
                Diary(
                    "#AD7FFF",
                    LocalDateTime(2023,1,22,1,12,22),
                    "더미 4",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        Gallery(R.drawable.ic_bottom_home_select),
                        Gallery(R.drawable.ic_bottom_share_no_select)
                    )
                )
            )
            add(
                Diary(
                    "#DA6022",
                    LocalDateTime(2023,1,24,1,32,22),
                    "더미 5",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        Gallery(R.drawable.ic_bottom_share_no_select),
                        Gallery(R.drawable.ic_bottom_home_select)
                    )
                )
            )
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




