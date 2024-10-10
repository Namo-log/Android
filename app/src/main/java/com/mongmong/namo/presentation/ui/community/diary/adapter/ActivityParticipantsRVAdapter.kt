package com.mongmong.namo.presentation.ui.community.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimDiaryActivityParticipantsBinding
import com.mongmong.namo.domain.model.ParticipantInfo

class ActivityParticipantsRVAdapter(
    private val scheduleParticipants: List<ParticipantInfo>,
    private val hasDiary: Boolean,
    private val isEdit: Boolean
) : RecyclerView.Adapter<ActivityParticipantsRVAdapter.ViewHolder>() {

    private val selectedParticipants = mutableListOf<ParticipantInfo>()

    fun addSelectedItems(participants: List<ParticipantInfo>) {
        selectedParticipants.clear()
        selectedParticipants.addAll(participants)
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
    fun getSelectedParticipants(): List<ParticipantInfo> {
        return selectedParticipants
    }

    inner class ViewHolder(val binding: ItemMoimDiaryActivityParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: ParticipantInfo) {
            binding.itemActivityParticipantsNicknameTv.text = participant.nickname
            binding.hasDiary = hasDiary
            binding.isEdit = isEdit

            // 초기화 시에 activityParticipants에 포함된 항목을 체크
            binding.itemActivityParticipantsCheckbox.isChecked = selectedParticipants.contains(participant)

            binding.itemActivityParticipantsCheckbox.setOnClickListener {
                if (binding.itemActivityParticipantsCheckbox.isChecked) {
                    selectedParticipants.add(participant)
                } else {
                    selectedParticipants.remove(participant)
                }
            }
        }
    }
}
