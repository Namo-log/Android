package com.mongmong.namo.presentation.ui.bottom.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.custom.Palette
import com.mongmong.namo.presentation.ui.bottom.custom.adapter.PaletteRVAdapter
import com.mongmong.namo.databinding.FragmentCustomPaletteBinding

class CustomPaletteFragment : Fragment() {

    lateinit var binding : FragmentCustomPaletteBinding
    lateinit var palette1ColorArr : IntArray
//    private var paletteDatas = ArrayList<Palette>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        binding = FragmentCustomPaletteBinding.inflate(inflater, container, false)

        palette1ColorArr = resources.getIntArray(R.array.palette1ColorArr)

        // 팔레트 색 Arr 넣어주기
        val paletteDatas = arrayListOf(
            Palette("기본 팔레트", palette1ColorArr)
        )

        //어댑터 연결
        binding.customPaletteRv.apply {
            adapter = PaletteRVAdapter(requireContext()).build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }


        return binding.root
    }

}