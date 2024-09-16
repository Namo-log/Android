package com.mongmong.namo.presentation.ui.community.friend

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.databinding.DialogFriendInfoBinding
import com.mongmong.namo.domain.model.Friend
import com.mongmong.namo.presentation.ui.community.CommunityCalendarActivity

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

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

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
            // 친구 일정 캘린더로 이동
            startActivity(
                Intent(requireActivity(), CommunityCalendarActivity::class.java)
                    .putExtra("friend", friendInfo)
            )
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
