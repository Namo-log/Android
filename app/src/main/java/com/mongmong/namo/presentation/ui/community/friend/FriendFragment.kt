package com.mongmong.namo.presentation.ui.community.friend

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentFriendBinding
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.ui.community.friend.adapter.FriendRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendFragment : BaseFragment<FragmentFriendBinding>(R.layout.fragment_friend), OnFriendInfoChangedListener {

    private val viewModel: FriendViewModel by viewModels()

    private lateinit var friendAdapter: FriendRVAdapter

    override fun setup() {
        binding.viewModel = this@FriendFragment.viewModel

        viewModel.getFriends()
        setAdapter()
        initClickListeners()
        initObserve()
    }

    override fun onResume() {
        super.onResume()

        viewModel.getFriends() // 친구 목록 조회
    }

    private fun initClickListeners() {
        // + 버튼
        binding.friendAddFloatingBtn.setOnClickListener {
            // 친구 추가 다이얼로그
            AddFriendDialog().show(parentFragmentManager, "AddFriendDialog")
        }
    }

    private fun setAdapter() {
        friendAdapter = FriendRVAdapter()
        binding.friendListRv.apply {
            adapter = friendAdapter
            layoutManager = LinearLayoutManager(context)
        }
        friendAdapter.setItemClickListener(object : FriendRVAdapter.MyItemClickListener {
            override fun onFavoriteButtonClick(position: Int) {
                // 즐겨찾기 상태 변경
                viewModel.toggleFriendFavoriteState(viewModel.friendList.value!![position].userId)
            }

            override fun onItemClick(position: Int) {
                // 친구 정보 화면으로 이동
                FriendInfoDialog(viewModel.friendList.value!![position], null, false, this@FriendFragment).show(parentFragmentManager, "FriendInfoDialog")
            }
        })
    }

    private fun initObserve() {
        viewModel.friendList.observe(viewLifecycleOwner) { friendList ->
            if (friendList.isNotEmpty()) {
                friendAdapter.addFriend(friendList)
            }
        }

        viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) viewModel.getFriends()
        }
    }

    override fun onFriendInfoChanged() {
        viewModel.getFriends() // 친구 목록 업데이트
    }
}