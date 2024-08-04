package com.mongmong.namo.presentation.ui.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentCustomBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.custom.adapter.CustomVPAdapter


class CustomFragment : BaseFragment<FragmentCustomBinding>(R.layout.fragment_custom) {
    private val information = arrayListOf("팔레트", "폰트", "MY")

    override fun setup() {
        setVPAdapter()
        onClickListener()
    }

    private fun onClickListener() {
        binding.apply {
            customSettingIv.setOnClickListener {
                view?.findNavController()?.navigate(R.id.action_customFragment_to_customSettingFragment)
            }
        }
    }

    private fun setVPAdapter() {
        val customAdapter = CustomVPAdapter(this)
        binding.customContentVp.adapter = customAdapter

        TabLayoutMediator(binding.customContentTb, binding.customContentVp){
                tab, position->
            tab.text= information[position]  //포지션에 따른 텍스트
        }.attach()  //탭레이아웃과 뷰페이저를 붙여주는 기능
    }
}