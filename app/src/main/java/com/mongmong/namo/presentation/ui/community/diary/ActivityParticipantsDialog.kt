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
import com.mongmong.namo.presentation.ui.community.diary.adapter.ActivityParticipantsRVAdapter


class ActivityParticipantsDialog(private val position: Int) : DialogFragment() {

    lateinit var binding: DialogActivityParticipantsBinding
    private lateinit var participantsAdapter: ActivityParticipantsRVAdapter

    private val viewModel: MoimDiaryViewModel by activityViewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogActivityParticipantsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initRecyclerView()
        initClickListener()
        initObserve()

        return binding.root
    }

    private fun initRecyclerView() {
        participantsAdapter = ActivityParticipantsRVAdapter(
            scheduleParticipants = viewModel.diarySchedule.value?.participantInfo ?: emptyList(),
            hasDiary = viewModel.diarySchedule.value?.hasDiary ?: false,
            isEdit = viewModel.isEditMode.value ?: false
        )
        binding.activityParticipantsRv.adapter = participantsAdapter.apply {
            addSelectedItems(viewModel.activities.value?.get(position)?.participants ?: emptyList())
        }
    }

    private fun initClickListener() {
        binding.activityPaymentSaveTv.setOnClickListener {
            val activityId = viewModel.activities.value?.get(position)?.activityId
            if(activityId == 0L) {
                viewModel.updateActivityParticipants(position, participantsAdapter.getSelectedParticipants())
                dismiss()
            } else {
                val originalParticipants = viewModel.activities.value?.get(position)?.participants ?: emptyList()
                val selectedParticipants = participantsAdapter.getSelectedParticipants()

                // 추가된 참가자
                val participantsToAdd = selectedParticipants
                    .filterNot { originalParticipants.contains(it) }
                    .map { it.userId }

                // 삭제된 참가자
                val participantsToRemove = originalParticipants
                    .filterNot { selectedParticipants.contains(it) }
                    .map { it.userId }

                viewModel.editActivityParticipants(activityId!!, participantsToAdd, participantsToRemove)
            }
        }

        // 취소 버튼 클릭 시
        binding.activityPaymentBackTv.setOnClickListener {
            dismiss()
        }
    }

    private fun initObserve() {
        viewModel.editActivityParticipantsResult.observe(viewLifecycleOwner) { response ->
            if(response.isSuccess) {
                viewModel.updateActivityParticipants(position, participantsAdapter.getSelectedParticipants())
                dismiss()
            } else Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
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
