package com.mongmong.namo.presentation.ui.community.friend

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentFriendBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.friend.adapter.FriendRVAdapter

class FriendFragment : BaseFragment<FragmentFriendBinding>(R.layout.fragment_friend) {

    private val viewModel: FriendViewModel by viewModels()

    private lateinit var friendAdapter: FriendRVAdapter

    override fun setup() {
        binding.apply {
            viewModel = this@FriendFragment.viewModel
            lifecycleOwner = this@FriendFragment
        }

        initObserve()
    }

    private fun setAdapter() {
        friendAdapter = FriendRVAdapter()
        binding.friendListRv.apply {
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(context)
        }
        friendAdapter.setItemClickListener(object : FriendRVAdapter.MyItemClickListener {
            override fun onFavoriteButtonClick(position: Int) {
                //TODO: 즐겨찾기 진행
            }

            override fun onItemClick(position: Int) {
                //TODO: 친구 정보 화면으로 이동
            }
        })
    }

    private fun initObserve() {
        viewModel.friendList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                setAdapter()
                friendAdapter.addFriend(it)
            }
        }
    }
}