package com.mongmong.namo.presentation.ui.community

import android.content.Intent
import com.google.android.material.tabs.TabLayoutMediator
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentCommunityBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.alert.FriendAlertActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding>(R.layout.fragment_community) {

    private val information = arrayListOf("모임 일정", "친구 리스트")

    override fun setup() {
        setVPAdapter()
        initClickListeners()
    }

    private fun initClickListeners() {
        binding.communityAlertIv.setOnClickListener {
            startActivity(Intent(requireActivity(), FriendAlertActivity::class.java))
        }
    }

    private fun setVPAdapter() {
        val communityAdapter = CommunityVPAdapter(this)
        binding.communityVp.adapter = communityAdapter

        TabLayoutMediator(binding.communityTb, binding.communityVp){
                tab, position->
            tab.text= information[position] // 포지션에 따른 텍스트 매칭
        }.attach()  // 탭레이아웃과 뷰페이저를 붙여주는 기능
    }
}