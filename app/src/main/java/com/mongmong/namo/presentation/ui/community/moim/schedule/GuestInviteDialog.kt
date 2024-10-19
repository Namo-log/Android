package com.mongmong.namo.presentation.ui.community.moim.schedule

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mongmong.namo.databinding.DialogGuestInviteBinding

class GuestInviteDialog : DialogFragment() {
    private val viewModel: MoimScheduleViewModel by activityViewModels()
    private lateinit var binding: DialogGuestInviteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGuestInviteBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initViews()
        initClickListeners()
        return binding.root
    }

    private fun initViews() {
        binding.guestInviteLinkTv.text = viewModel.guestInvitationLink
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭 시
        binding.guestInviteCloseTv.setOnClickListener {
            dismiss()
        }

        // 링크 복사 버튼
        binding.guestInviteCopyLinkBtn.setOnClickListener {
            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("copy pass", viewModel.guestInvitationLink)
            clipboardManager.setPrimaryClip(clip) //클립보드에 데이터 set
            Toast.makeText(requireContext(), "클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show()
        }

        // 외부 공유 버튼
        binding.guestInviteShareBtn.setOnClickListener {
            //TODO: 카카오톡, 메시지로 공유
        }
    }
}
