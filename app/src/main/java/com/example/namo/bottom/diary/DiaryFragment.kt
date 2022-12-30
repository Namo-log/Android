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

        val currentTime = Calendar.getInstance()
        val year = currentTime.get(Calendar.YEAR)
        val month = currentTime.get(Calendar.MONTH)+1

        binding.diaryMonth.text = "${year}.${month}"

        dummy()

        binding.diaryMonth.setOnClickListener {
            view?.let { it1 -> dialogCreate(it1) }
        }

//        binding.diaryMonth.setOnClickedListener (object : YearMonthDialog.ButtonClickListener {
//                override fun onClicked(year:Int,month:Int) {
//                binding.diaryMonth.text = "${year}.${month}"
//            }
//        })

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        initRecyclerview()
    }

    private fun dialogCreate(view: View) {
        val pd: YearMonthDialog= YearMonthDialog(view)
        pd.show(parentFragmentManager, "YearMonthPickerTest")

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
                    "2022-12-28",
                    "더미 1",
                    "nnnnnnnnnnnnnnnnnn",
                    mutableListOf(GalleryDummy(R.drawable.ic_division))
                )
            )
            add(
                DiaryDummy(
                    "#E1B000",
                    "2022-12-29",
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
                    "2023-1-28",
                    "더미 3",
                    "nnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        GalleryDummy(R.drawable.ic_login_kakao),
                        GalleryDummy(R.drawable.ic_gallery),
                        GalleryDummy(R.drawable.ic_gallery)
                    )
                )
            )
            add(
                DiaryDummy(
                    "#AD7FFF",
                    "2022-11-28",
                    "더미 4",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        GalleryDummy(R.drawable.ic_gallery),
                        GalleryDummy(R.drawable.ic_login_naver)
                    )
                )
            )
            add(
                DiaryDummy(
                    "#DA6022",
                    "2023-2-28",
                    "더미 5",
                    "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                    mutableListOf(
                        GalleryDummy(R.drawable.ic_gallery),
                        GalleryDummy(R.drawable.ic_gallery)
                    )
                )
            )
        }
    }

}

    data class DiaryDummy(
        var category: String,
        var date: String,
        var title: String,
        var contents: String,
        var rv: MutableList<GalleryDummy>
    )

    data class GalleryDummy(
        var img: Int
    )

