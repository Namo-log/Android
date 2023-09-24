package com.example.namo.ui.bottom.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.data.entity.custom.Palette
import com.example.namo.databinding.FragmentCustomMyBinding
import com.example.namo.ui.bottom.custom.adapter.PaletteRVAdapter

class CustomMyFragment : Fragment() {

    lateinit var binding : FragmentCustomMyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomMyBinding.inflate(inflater, container, false)

        // 팔레트 페이지랑 동일한 데이터
        val paletteDatas = arrayListOf(
            Palette("기본 팔레트",
                arrayListOf(
                    R.color.palette1, R.color.palette2, R.color.palette3, R.color.palette4, R.color.palette5,
                    R.color.palette6, R.color.palette7, R.color.palette8, R.color.palette9, R.color.palette10)),
        )

        //어댑터 연결
        binding.customMyPaletteRv.apply {
            adapter = PaletteRVAdapter(requireContext()).build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        return binding.root
    }

}