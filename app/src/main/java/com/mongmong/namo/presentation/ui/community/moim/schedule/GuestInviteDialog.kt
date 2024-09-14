package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

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
