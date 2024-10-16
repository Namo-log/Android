package com.mongmong.namo.presentation.ui.community.diary

import android.annotation.SuppressLint
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
import com.mongmong.namo.databinding.DialogActivityParticipantsBinding
import com.mongmong.namo.databinding.DialogMoimPaymentBinding
import com.mongmong.namo.presentation.ui.community.diary.adapter.ActivityParticipantsRVAdapter
import com.mongmong.namo.presentation.ui.community.diary.adapter.MoimPaymentParticipantsRVAdapter


class MoimPaymentDialog() : DialogFragment() {

    lateinit var binding: DialogMoimPaymentBinding
    private lateinit var participantsAdapter: MoimPaymentParticipantsRVAdapter

    private val viewModel: MoimDiaryViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMoimPaymentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initRecyclerView()
        initClickListener()

        return binding.root
    }

    private fun initRecyclerView() {
        participantsAdapter = MoimPaymentParticipantsRVAdapter(
            viewModel.moimPayment.value?.moimPaymentParticipants ?: emptyList())
        binding.moimPaymentRv.adapter = participantsAdapter
    }

    private fun initClickListener() {
        binding.moimPaymentBackTv.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
