package com.mongmong.namo.presentation.ui.community.diary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimDiaryParticipantBinding
import com.mongmong.namo.domain.model.ParticipantInfo

class MoimDiaryParticipantsRVAdapter() : RecyclerView.Adapter<MoimDiaryParticipantsRVAdapter.ViewHolder>() {
    private val members = mutableListOf<ParticipantInfo>()
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newMembers: List<ParticipantInfo>) {
        members.clear()
        members.addAll(newMembers)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimDiaryParticipantBinding = ItemMoimDiaryParticipantBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemMoimDiaryParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: ParticipantInfo) {
            binding.peopleNameTv.text = member.nickname
        }
    }
}
