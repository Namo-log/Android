package com.gradu.presentation.community.moim.schedule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.presentation.databinding.ItemMoimParticipantBinding

class MoimParticipantRVAdapter(private val participantList : List<com.gradu.domain.model.Participant>) : RecyclerView.Adapter<MoimParticipantRVAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding : ItemMoimParticipantBinding = ItemMoimParticipantBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(participantList[position])
    }

    override fun getItemCount(): Int = participantList.size

    inner class ViewHolder(val binding: ItemMoimParticipantBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(participant : com.gradu.domain.model.Participant) {
            binding.participant = participant
        }
    }
}