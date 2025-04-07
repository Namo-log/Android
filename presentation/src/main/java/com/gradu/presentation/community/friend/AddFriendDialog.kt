package com.gradu.presentation.ui.community.friend

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.mongmong.namo.databinding.DialogAddFriendBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddFriendDialog : DialogFragment() {
    private lateinit var binding: DialogAddFriendBinding
    private val viewModel: FriendViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddFriendBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initObserve()
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
            viewModel.requestFriend(binding.addFriendNicknameTagEt.text.toString())
        }
    }

    private fun initObserve() {
        viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) dismiss()
        }
    }
}
