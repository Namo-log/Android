package com.mongmong.namo.presentation.ui.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.databinding.ItemMoimFriendBinding

class MoimParticipantRVAdapter(private val memberList : List<GroupMember>) : RecyclerView.Adapter<MoimParticipantRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding : ItemMoimFriendBinding = ItemMoimFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    private fun isChef(position: Int): Boolean {
        //TODO: 방장 조건 추가
        return position == 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(memberList[position])
    }

    override fun getItemCount(): Int = memberList.size

    inner class ViewHolder(val binding: ItemMoimFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend : GroupMember) {
            binding.participant = friend
            binding.isChief = isChef(bindingAdapterPosition)
            binding.itemMoimFriendDeleteIv.visibility = if (isChef(adapterPosition)) View.GONE else View.VISIBLE
        }
    }
}