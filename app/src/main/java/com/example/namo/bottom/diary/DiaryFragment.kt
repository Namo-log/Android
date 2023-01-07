package com.example.namo.bottom.diary

import YearMonthDialog
import YearMonthDialog.Companion.month
import YearMonthDialog.Companion.year
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.databinding.FragmentDiaryBinding
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class DiaryFragment: Fragment() {

    lateinit var binding: FragmentDiaryBinding
    private var diaryDatas = ArrayList<DiaryDummy>()


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

        val now=LocalDate.now()
        val years=now.format(DateTimeFormatter.ofPattern("YYYY"))
        val months=now.format(DateTimeFormatter.ofPattern("MM"))

        binding.diaryMonth.text="$years.$months"

        initRecyclerview()

    }

    private fun dialogCreate() {

        val months=DecimalFormat("00")
        val dialog =
           YearMonthDialog.getInstance(acceptClick = {
                  binding.diaryMonth.text= "$year.${months.format(month)}"
            }, month = month, year =year)
        dialog.show(parentFragmentManager,"YearMonthPickerTest")
    }


    private fun initRecyclerview() {
        binding.diaryListRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val listAdapter = DiaryListRVAdapter(requireContext(), diaryDatas)
        binding.diaryListRv.adapter = listAdapter
    }


    fun dummy() {

        diaryDatas.apply {
            add(
                DiaryDummy(
                    "#DE8989",
                    2022,11,9,
                    "더미 1",
                    "nnnnnnnnnnnnnnnnnn",
                    mutableListOf(GalleryDummy(R.drawable.ic_division))
                )
            )
            add(
                DiaryDummy(
                    "#E1B000",
                    2023,1,7,
                    "더미 2",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        GalleryDummy(R.drawable.ic_gallery)
                    )
                )
            )
            add(
                DiaryDummy(
                    "#5C8596",
                    2023,2,3,
                    "더미 3",
                    "nnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        GalleryDummy(R.drawable.ic_login_kakao),
                        GalleryDummy(R.drawable.ic_gallery),
                        GalleryDummy(R.drawable.ic_gallery)
                    )
                )
            ) } } }





