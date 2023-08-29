package com.example.namo.ui.bottom.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.custom.Palette
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
        val paletteDatas = arrayListOf(
            Palette("기본 팔레트",
                arrayListOf(
                    R.color.palette1, R.color.palette2, R.color.palette3, R.color.palette4, R.color.palette5,
                    R.color.palette6, R.color.palette7, R.color.palette8, R.color.palette9, R.color.palette10)),
        )

        //어댑터 연결
        binding.customPaletteRv.apply {
            adapter = PaletteRVAdapter(requireContext()).build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }


        return binding.root
    }

}