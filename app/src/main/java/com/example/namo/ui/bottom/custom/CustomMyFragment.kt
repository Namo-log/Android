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
    lateinit var palette1ColorArr : IntArray

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomMyBinding.inflate(inflater, container, false)

        palette1ColorArr = resources.getIntArray(R.array.palette1ColorArr)

        // 팔레트 페이지랑 동일한 데이터
        val paletteDatas = arrayListOf(
            Palette("기본 팔레트", palette1ColorArr)
        )

        //어댑터 연결
        binding.customMyPaletteRv.apply {
            adapter = PaletteRVAdapter(requireContext()).build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        return binding.root
    }

}