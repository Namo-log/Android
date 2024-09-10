package com.mongmong.namo.presentation.ui.community.moim.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mongmong.namo.databinding.DialogMoimParticipantBinding
import com.mongmong.namo.presentation.ui.community.moim.MoimViewModel
import com.mongmong.namo.presentation.ui.community.moim.calendar.adapter.ParticipantInfoRVAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoimParticipantDialog : DialogFragment() {
    private val viewModel: MoimViewModel by activityViewModels()
    private lateinit var binding: DialogMoimParticipantBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogMoimParticipantBinding.inflate(inflater, container, false)

        setAdapter()
        initClickListeners()
        return binding.root
    }

    private fun setAdapter() {
        val moimInfo = viewModel.moim
        val participantInfoRVAdapter = ParticipantInfoRVAdapter(moimInfo.getParticipantsColoInfo())
        binding.moimParticipantRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = participantInfoRVAdapter
        }
    }

    private fun initClickListeners() {
        // 닫기 버튼 클릭 시
        binding.moimParticipantCloseTv.setOnClickListener {
            dismiss()
        }
    }
}
