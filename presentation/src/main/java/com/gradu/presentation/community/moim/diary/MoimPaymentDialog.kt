package com.gradu.presentation.community.moim.diary

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gradu.presentation.databinding.DialogMoimPaymentBinding
import com.gradu.presentation.community.moim.diary.adapter.MoimPaymentParticipantsRVAdapter
import androidx.core.graphics.drawable.toDrawable


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

        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initRecyclerView()
        initClickListener()

        return binding.root
    }

    private fun initRecyclerView() {
        Log.d("MoimPaymentDialog", "${viewModel.moimPayment.value?.moimPaymentParticipants}")
        participantsAdapter = MoimPaymentParticipantsRVAdapter(
            viewModel.moimPayment.value?.moimPaymentParticipants ?: emptyList()
        )
        binding.moimPaymentRv.apply {
            adapter = participantsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
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
