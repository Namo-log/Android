package com.gradu.presentation.community.moim.diary

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.gradu.domain.model.ActivityParticipant
import com.gradu.presentation.community.moim.diary.adapter.ActivityParticipantsRVAdapter
import com.gradu.presentation.databinding.DialogActivityParticipantsBinding
import androidx.core.graphics.drawable.toDrawable


class ActivityParticipantsDialog(private val position: Int) : DialogFragment() {

    lateinit var binding: DialogActivityParticipantsBinding
    private lateinit var participantsAdapter: ActivityParticipantsRVAdapter

    private val viewModel: MoimDiaryViewModel by activityViewModels()
    private lateinit var activity: com.gradu.domain.model.Activity

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogActivityParticipantsBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        activity = viewModel.activities.value?.get(position)!!


        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())  // 배경 투명하게
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)  // dialog 모서리 둥글게

        initRecyclerView()
        initClickListener()
        initObserve()

        return binding.root
    }

    private fun initRecyclerView() {
        participantsAdapter = ActivityParticipantsRVAdapter(
            scheduleParticipants =
            viewModel.diarySchedule.value?.participantInfo?.map {
                ActivityParticipant(
                    participantId = it.participantId,
                    activityParticipantId = 0L,
                    nickname = it.nickname
                )
            } ?: emptyList(),
            hasDiary = viewModel.diarySchedule.value?.hasDiary ?: false,
            isEdit = viewModel.isEditMode.value ?: false
        )
        binding.activityParticipantsRv.adapter = participantsAdapter
        participantsAdapter.addSelectedItems(viewModel.activities.value?.get(position)?.participants ?: emptyList())
    }

    private fun initClickListener() {
        binding.activityPaymentSaveTv.setOnClickListener {
            val originalParticipants = activity.participants
            val selectedParticipants = participantsAdapter.getSelectedParticipants()

            // 추가된 참가자
            val participantsToAdd = selectedParticipants
                .filterNot { selected ->
                    originalParticipants.any { it.participantId == selected.participantId }
                }.map { it.participantId }

            // 삭제된 참가자
            val participantsToRemove = originalParticipants
                .filterNot { original ->
                    selectedParticipants.any { it.participantId == original.participantId }
                }.map { it.participantId }

            Log.d("activityPaymentSaveTv", "${originalParticipants.map { it.participantId }}, ${selectedParticipants.map { it.participantId }} \n ${participantsToAdd}, ${participantsToRemove}")

            if (activity.activityId == 0L) { // 새로운 활동인 경우
                viewModel.updateActivityParticipants(position, selectedParticipants)

                // 기존의 정산 참가자에서 삭제된 참가자 제거
                val updatedPaymentParticipants = activity.payment.participants.filterNot { participant ->
                    participantsToRemove.contains(participant.id)
                }.toMutableList()

                // 추가된 참가자를 활동 정산에 추가
                val newPaymentParticipants = participantsToAdd.map { participantId ->
                    com.gradu.domain.model.PaymentParticipant(
                        id = participantId,
                        nickname = selectedParticipants.find { it.participantId == participantId }?.nickname
                            ?: "",
                        isPayer = false
                    )
                }
                updatedPaymentParticipants.addAll(newPaymentParticipants)

                // 활동 정산 참가자 업데이트
                val updatedPayment = activity.payment.copy(participants = updatedPaymentParticipants)
                viewModel.updateActivityPayment(position, updatedPayment)
                // 모임 전체 정산 업데이트
                viewModel.setTotalMoimPayment()
                dismiss()
            } else {
                // 이미 존재하는 활동인 경우 참가자 수정 api 호출
                viewModel.editActivityParticipants(activity.activityId, participantsToAdd, participantsToRemove)
            }
        }

        // 취소 버튼 클릭 시
        binding.activityPaymentBackTv.setOnClickListener {
            dismiss()
        }
    }


    private fun initObserve() {
        lifecycleScope.launchWhenStarted {
            viewModel.editActivityParticipantsResult.collect { response ->
                Log.d("ActivityParticipantsDialog", "$response")
                if (response.isSuccess) {
                    viewModel.updateActivityParticipants(
                        position,
                        participantsAdapter.getSelectedParticipants()
                    )
                    // 활동 참가자 수정 완료 후 활동 정산 참가자 갱신
                    viewModel.getActivityPayment(activity?.activityId!!, position)
                    // 모임 전체 정산 업데이트
                    viewModel.setTotalMoimPayment()
                    dismiss()
                } else Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
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
