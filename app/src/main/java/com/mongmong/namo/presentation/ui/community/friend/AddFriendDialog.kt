package com.mongmong.namo.presentation.ui.community.friend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.databinding.DialogAddFriendBinding

class AddFriendDialog : DialogFragment() {
    private lateinit var binding: DialogAddFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddFriendBinding.inflate(inflater, container, false)

        initClickListeners()
        return binding.root
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭
        binding.addFriendCloseIv.setOnClickListener {
            dismiss()
        }

        // 친구 추가 버튼
        binding.addFriendRequestBtn.setOnClickListener {
            //TODO: 친구 신청 진행
            Toast.makeText(requireContext(), "친구 신청 버튼 클릭", Toast.LENGTH_SHORT).show()
        }
    }
}
