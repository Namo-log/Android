package com.gradu.presentation.community.moim.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.domain.model.MoimPaymentParticipant
import com.gradu.presentation.databinding.ItemMoimPaymentParticipantBinding

class MoimPaymentParticipantsRVAdapter(
    private val participants: List<MoimPaymentParticipant>
) : RecyclerView.Adapter<MoimPaymentParticipantsRVAdapter.ParticipantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemMoimPaymentParticipantBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(participants[position])
    }

    override fun getItemCount(): Int = participants.size

    inner class ParticipantViewHolder(
        private val binding: ItemMoimPaymentParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(participant: MoimPaymentParticipant) {
            binding.participant = participant
            binding.executePendingBindings()
        }
    }
}
