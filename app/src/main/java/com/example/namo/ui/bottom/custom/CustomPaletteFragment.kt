package com.example.namo.ui.bottom.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.ui.bottom.custom.adapter.PaletteRVAdapter
import com.example.namo.databinding.FragmentCustomPaletteBinding

class CustomPaletteFragment : Fragment() {

    lateinit var binding : FragmentCustomPaletteBinding
//    private var paletteDatas = ArrayList<Palette>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentCustomPaletteBinding.inflate(inflater, container, false)

        //더미데이터 냅다 집어 넣기
        var paletteDatas = arrayListOf<Palette>(
            Palette("기본 팔레트",
                arrayListOf("#EB5353", "#EC9B3B", "#FBCB0A", "#96BB7C", "#5A8F7B",
                    "#82C4C3", "#187498", "#8571BF", "#E36488", "#858585")),
//            Palette(
//                "테스트 1",
//                arrayListOf("#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC",
//                    "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC")
//            ),
//            Palette(
//                "테스트 2",
//                arrayListOf("#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC",
//                    "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC", "#CCCCCC")
//            )
        )

        //어댑터 연결
        binding.customPaletteRv.apply {
            adapter = PaletteRVAdapter().build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }


        return binding.root
    }

}