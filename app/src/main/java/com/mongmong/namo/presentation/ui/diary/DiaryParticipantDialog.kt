package com.mongmong.namo.presentation.ui.diary

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mongmong.namo.databinding.DialogDiaryParticipantBinding

class DiaryParticipantDialog(
    private val participantCount: Int,
    private val participantNames: String
) : DialogFragment() {
    private lateinit var binding: DialogDiaryParticipantBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogDiaryParticipantBinding.inflate(inflater, container, false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 닫기 버튼 클릭 시
        binding.diaryParticipantBackTv.setOnClickListener {
            dismiss()
        }

        binding.diaryParticipantTotalNumTv.text = participantCount.toString()
        binding.diaryParticipantParticipantTv.text = participantNames

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
