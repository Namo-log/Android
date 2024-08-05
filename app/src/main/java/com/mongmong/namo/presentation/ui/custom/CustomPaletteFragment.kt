package com.mongmong.namo.presentation.ui.custom

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.custom.Palette
import com.mongmong.namo.databinding.FragmentCustomPaletteBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.config.PaletteType
import com.mongmong.namo.presentation.ui.custom.adapter.PaletteRVAdapter

class CustomPaletteFragment : BaseFragment<FragmentCustomPaletteBinding>(R.layout.fragment_custom_palette) {
    override fun setup() {
        // 팔레트 색 Arr 넣어주기
        val paletteDatas = arrayListOf(
            Palette("기본 팔레트", CategoryColor.findColorsByPaletteType(PaletteType.BASIC_PALETTE))
        )

        //어댑터 연결
        binding.customPaletteRv.apply {
            adapter = PaletteRVAdapter(requireContext()).build(paletteDatas)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }
}