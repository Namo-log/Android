package com.mongmong.namo.presentation.ui.group.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.databinding.ItemGroupMemberBinding
import com.mongmong.namo.presentation.config.CategoryColor

class GroupInfoMemberRVAdapter(
    private val members : List<GroupMember>
) : RecyclerView.Adapter<GroupInfoMemberRVAdapter.ViewHolder>() {

    private lateinit var context : Context
    private lateinit var categoryColorArray : ArrayList<String>

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupInfoMemberRVAdapter.ViewHolder {
        val binding = ItemGroupMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        categoryColorArray = CategoryColor.getAllColors()

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupInfoMemberRVAdapter.ViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size


    inner class ViewHolder(val binding : ItemGroupMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member : GroupMember) {
            binding.groupMember = member
        }
    }

}