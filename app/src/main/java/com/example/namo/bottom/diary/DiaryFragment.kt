package com.example.namo.bottom.diary

import YearMonthDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.databinding.FragmentDiaryBinding
import org.joda.time.DateTime
import java.util.*


class DiaryFragment: Fragment() {

    lateinit var binding: FragmentDiaryBinding

    private var diaryData = ArrayList<Diary>()
    private var millis = DateTime().withDayOfMonth(1).withTimeAtStartOfDay().millis

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDiaryBinding.inflate(inflater, container, false)
        dummy()

        binding.diaryMonth.setOnClickListener {
            dialogCreate()
        }

        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        binding.diaryMonth.text=DateTime(millis).toString("yyyy.MM")
        initRecyclerview()
    }

    private fun dialogCreate() {

        YearMonthDialog(millis){
            binding.diaryMonth.text=DateTime(it).toString("yyyy.MM")
        }.show(parentFragmentManager,"test")
    }


    private fun initRecyclerview() {

        binding.diaryListRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val listAdapter = DiaryListRVAdapter(requireContext(), diaryData)
        binding.diaryListRv.adapter = listAdapter
          }

    fun dummy() {

        diaryData.apply {
            add(
                Diary(
                    "#DE8989",
                    1673254515000,
                    "더미 1",
                    "nnnnnnnnnnnnnnnnnn",
                    mutableListOf(Gallery(R.drawable.bg_gradient_splash))
                )
            )
            add(
                Diary(
                    "#E1B000",
                    1672563315000,
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
                    1673686515000,
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
                    1673686000000,
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
                    1673513715000,
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


}




