package com.mongmong.namo.presentation.ui.group.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.domain.model.group.GroupMember
import com.mongmong.namo.databinding.ItemGroupScheduleMemberBinding

class GroupScheduleMemberRVAdapter(var context: Context, private val memberList : List<GroupMember>) : RecyclerView.Adapter<GroupScheduleMemberRVAdapter.ViewHolder>() {

    private val selectedMembers = ArrayList<Boolean>()

    init {
        for (i in memberList.indices) {
            selectedMembers.add(false)
        }
    }

    fun setMemberSelected(position: Int, isSelected:Boolean) {
        selectedMembers[position] = isSelected
    }

    fun getSelectedMembers(): List<GroupMember> {
        val selectedList = ArrayList<GroupMember>()
        for (i in memberList.indices) {
            if (selectedMembers[i]) {
                selectedList.add(memberList[i])
            }
        }

        return selectedList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding : ItemGroupScheduleMemberBinding = ItemGroupScheduleMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList[position]

        holder.bind(member)

        holder.binding.groupScheduleMemberCheckBox.isChecked = selectedMembers[position]
        holder.binding.groupScheduleMemberCheckBox.setOnCheckedChangeListener { _, isChecked ->
            setMemberSelected(position, isChecked)
        }
    }

    override fun getItemCount(): Int = memberList.size

    fun setSelectedMember(list : ArrayList<Boolean>) {
        selectedMembers.clear()
        selectedMembers.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemGroupScheduleMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(member : GroupMember) {
            binding.groupScheduleMemberNameTv.text = member.userName
        }
    }
}