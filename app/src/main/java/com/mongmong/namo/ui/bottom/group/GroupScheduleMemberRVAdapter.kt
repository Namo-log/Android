package com.mongmong.namo.ui.bottom.group

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.mongmong.namo.data.remote.moim.MoimUser
import com.mongmong.namo.databinding.ItemGroupScheduleMemberBinding

class GroupScheduleMemberRVAdapter(var context: Context, private val memberList : List<MoimUser>) : RecyclerView.Adapter<GroupScheduleMemberRVAdapter.ViewHolder>() {

    private val selectedMembers = ArrayList<Boolean>()

    init {
        for (i in memberList.indices) {
            selectedMembers.add(false)
        }
    }

    fun setMemberSelected(position: Int, isSelected:Boolean) {
        selectedMembers[position] = isSelected
    }

    fun getSelectedMembers(): List<MoimUser> {
        val selectedList = ArrayList<MoimUser>()
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
    ): GroupScheduleMemberRVAdapter.ViewHolder {
        val binding : ItemGroupScheduleMemberBinding = ItemGroupScheduleMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        context = parent.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupScheduleMemberRVAdapter.ViewHolder, position: Int) {
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
        fun bind(member : MoimUser) {
            binding.groupScheduleMemberNameTv.text = member.userName
        }
    }
}