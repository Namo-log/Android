package com.mongmong.namo.presentation.ui.community.alert

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentFriendAlertBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.alert.adapter.FriendAlertRVAdapter
import com.mongmong.namo.presentation.ui.community.friend.FriendInfoDialog

class FriendAlertFragment : BaseFragment<FragmentFriendAlertBinding>(R.layout.fragment_friend_alert) {

    private val viewModel: AlertViewModel by activityViewModels()

    private lateinit var friendAdapter: FriendAlertRVAdapter

    override fun setup() {
        binding.viewModel = this@FriendAlertFragment.viewModel

        initObserve()
    }

    private fun setAdapter() {
        friendAdapter = FriendAlertRVAdapter()
        binding.friendAlertListRv.apply {
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(context)
        }
        friendAdapter.setItemClickListener(object : FriendAlertRVAdapter.MyItemClickListener {
            override fun onFriendInfoClick(position: Int) {
                // 친구 정보 화면으로 이동
                FriendInfoDialog(viewModel.friendRequestList.value!![position], true).show(parentFragmentManager, "FriendInfoDialog")
            }

            override fun onAcceptBtnClick(position: Int) {
                //TODO: 요청 수락 진행
            }

            override fun onDenyBtnClick(position: Int) {
                //TODO: 요청 거절 진행
            }
        })
    }

    private fun initObserve() {
        viewModel.friendRequestList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setAdapter()
                friendAdapter.addRequest(it)
            }
        }
    }
}