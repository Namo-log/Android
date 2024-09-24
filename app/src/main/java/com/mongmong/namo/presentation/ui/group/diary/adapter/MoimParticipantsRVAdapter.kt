package com.mongmong.namo.presentation.ui.group.diary.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemMoimDiaryParticipantsBinding
import com.mongmong.namo.domain.model.group.MoimScheduleMember

class MoimParticipantsRVAdapter() : RecyclerView.Adapter<MoimParticipantsRVAdapter.ViewHolder>() {
    private val members = mutableListOf<MoimScheduleMember>()
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newMembers: List<MoimScheduleMember>) {
        members.clear()
        members.addAll(newMembers)
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemMoimDiaryParticipantsBinding = ItemMoimDiaryParticipantsBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemMoimDiaryParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: MoimScheduleMember) {
            binding.peopleNameTv.text = member.userName
        }
    }
}
