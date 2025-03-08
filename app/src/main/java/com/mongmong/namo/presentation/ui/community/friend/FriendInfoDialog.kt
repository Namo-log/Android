package com.mongmong.namo.presentation.ui.community.friend

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mongmong.namo.databinding.DialogFriendInfoBinding
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.domain.model.FriendRequest
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity
import dagger.hilt.android.AndroidEntryPoint

interface OnFriendInfoChangedListener {
    fun onFriendInfoChanged()
}

@AndroidEntryPoint
class FriendInfoDialog(
    private val friendInfo: Friend?,
    private val friendRequestInfo: FriendRequest?,
    private val isFriendRequestMode: Boolean, // 친구 요청 화면인지, 친구 리스트 화면인지 판단
    private val listener: OnFriendInfoChangedListener
) : DialogFragment() {

    private lateinit var binding: DialogFriendInfoBinding
    private val viewModel: FriendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFriendInfoBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        val info = friendInfo?.convertToFriendInfo() ?: friendRequestInfo!!.convertToFriendInfo()

        binding.apply {
            friendInfo = info
            isFavorite = this@FriendInfoDialog.friendInfo?.isFavorite ?: false
            isFriendRequestMode = this@FriendInfoDialog.isFriendRequestMode
        }

        initClickListeners()
        initObserve()
        return binding.root
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭
        binding.friendInfoCloseTv.setOnClickListener {
            dismiss()
        }

        // 즐겨찾기 버튼 클릭
        binding.friendInfoFavoriteIv.setOnClickListener {
            viewModel.toggleFriendFavoriteState(friendInfo!!.userId)
            binding.isFavorite = !binding.isFavorite!! // 즐겨찾기 여부 전환
        }

        // 친구 리스트 - 일정 보기 버튼 클릭
        binding.friendInfoScheduleBtn.setOnClickListener {
            // 친구 일정 캘린더로 이동
            startActivity(
                Intent(requireActivity(), CommunityCalendarActivity::class.java)
                    .putExtra("friend", friendInfo)
            )
        }

        // 친구 리스트 - 삭제 버튼 클릭
        binding.friendInfoDeleteBtn.setOnClickListener {
            viewModel.deleteFriend(friendInfo!!.userId)
        }

        // 친구 요청 - 수락 버튼 클릭
        binding.friendInfoRequestAcceptBtn.setOnClickListener {
            viewModel.acceptFriendRequest(friendRequestInfo!!.friendRequestId)
        }

        // 친구 요청 - 거절 버튼 클릭
        binding.friendInfoRequestDenyBtn.setOnClickListener {
            viewModel.denyFriendRequest(friendRequestInfo!!.friendRequestId)
        }
    }

    private fun initObserve() {
        viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                listener.onFriendInfoChanged() // 데이터 변동 존재
                dismiss()
            }
        }
    }
}
