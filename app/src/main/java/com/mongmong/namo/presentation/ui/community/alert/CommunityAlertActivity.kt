package com.mongmong.namo.presentation.ui.community.alert

import androidx.activity.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityCommunityAlertBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.mongmong.namo.presentation.ui.community.alert.adapter.CommunityAlertVPAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityAlertActivity : BaseActivity<ActivityCommunityAlertBinding>(R.layout.activity_community_alert) {

    private val information = arrayListOf("모임 요청", "친구 요청")
    private val viewModel: AlertViewModel by viewModels()

    override fun setup() {
        setVPAdapter()

        binding.apply {
            viewModel = this@CommunityAlertActivity.viewModel
            lifecycleOwner = this@CommunityAlertActivity
        }

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.communityAlertBackIv.setOnClickListener {
            // 뒤로가기
            finish()
        }
    }

    private fun setVPAdapter() {
        val alertAdapter = CommunityAlertVPAdapter(this)
        binding.communityAlertVp.adapter = alertAdapter

        TabLayoutMediator(binding.communityAlertTb, binding.communityAlertVp){
                tab, position->
            tab.text= information[position] // 포지션에 따른 텍스트 매칭
        }.attach()  // 탭레이아웃과 뷰페이저를 붙여주는 기능
    }
}