package com.gradu.presentation.community.moim.diary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.presentation.databinding.ItemMoimDiaryActivityParticipantsBinding

class ActivityParticipantsRVAdapter(
    private val scheduleParticipants: List<com.gradu.domain.model.ActivityParticipant>,
    private val hasDiary: Boolean,
    private val isEdit: Boolean
) : RecyclerView.Adapter<ActivityParticipantsRVAdapter.ViewHolder>() {

    private val selectedParticipants = mutableListOf<com.gradu.domain.model.ActivityParticipant>()

    @SuppressLint("NotifyDataSetChanged")
    fun addSelectedItems(participants: List<com.gradu.domain.model.ActivityParticipant>) {
        selectedParticipants.clear()
        selectedParticipants.addAll(participants)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimDiaryActivityParticipantsBinding = ItemMoimDiaryActivityParticipantsBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val participant = scheduleParticipants[position]
        holder.bind(participant)
    }

    override fun getItemCount(): Int = scheduleParticipants.size

    // 선택된 참가자 리스트 반환
    fun getSelectedParticipants(): List<com.gradu.domain.model.ActivityParticipant> {
        return selectedParticipants
    }

    inner class ViewHolder(val binding: ItemMoimDiaryActivityParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: com.gradu.domain.model.ActivityParticipant) {
            binding.itemActivityParticipantsNicknameTv.text = participant.nickname
            binding.hasDiary = hasDiary
            binding.isEdit = isEdit

            binding.itemActivityParticipantsCheckbox.isChecked =
                selectedParticipants.any { it.participantId == participant.participantId }

            binding.itemActivityParticipantsCheckbox.setOnClickListener {
                if (binding.itemActivityParticipantsCheckbox.isChecked) {
                    selectedParticipants.add(participant)
                } else {
                    // participantId를 기준으로 항목을 제거합니다.
                    selectedParticipants.removeAll { it.participantId == participant.participantId }
                }
            }

        }
    }
}
