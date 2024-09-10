package com.mongmong.namo.presentation.ui.community.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.databinding.DialogFriendInfoBinding
import com.mongmong.namo.domain.model.Friend

class FriendInfoDialog(
    private val friendInfo: Friend,
    private val isFriendRequestMode: Boolean, // 친구 요청 화면인지, 친구 리스트 화면인지 판단
) : DialogFragment() {
    private lateinit var binding: DialogFriendInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFriendInfoBinding.inflate(inflater, container, false)

        binding.apply {
            friend = friendInfo
            isFriendRequestMode = this@FriendInfoDialog.isFriendRequestMode
        }

        initClickListeners()
        return binding.root
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭
        binding.friendInfoCloseTv.setOnClickListener {
            dismiss()
        }

        // 친구 리스트 - 일정 보기 버튼 클릭
        binding.friendInfoScheduleBtn.setOnClickListener {
            //TODO: 친구 일정 캘린더로 이동
            Toast.makeText(requireContext(), "일정 버튼 클릭", Toast.LENGTH_SHORT).show()
        }

        // 친구 리스트 - 삭제 버튼 클릭
        binding.friendInfoDeleteBtn.setOnClickListener {
            //TODO: 친구 삭제 진행
            Toast.makeText(requireContext(), "삭제 버튼 클릭", Toast.LENGTH_SHORT).show()
        }

        // 친구 요청 - 수락 버튼 클릭
        binding.friendInfoRequestAcceptBtn.setOnClickListener {
            //TODO: 친구 요청 수락
            Toast.makeText(requireContext(), "친구 요청 수락", Toast.LENGTH_SHORT).show()
        }

        // 친구 요청 - 거절 버튼 클릭
        binding.friendInfoRequestDenyBtn.setOnClickListener {
            //TODO: 친구 요청 거절
            Toast.makeText(requireContext(), "친구 요청 거절", Toast.LENGTH_SHORT).show()
        }
    }
}
