package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.databinding.DialogGuestInviteBinding

class GuestInviteDialog : DialogFragment() {
    private lateinit var binding: DialogGuestInviteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGuestInviteBinding.inflate(inflater, container, false)

        initClickListeners()
        return binding.root
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭 시
        binding.guestInviteCloseTv.setOnClickListener {
            dismiss()
        }
    }
}
