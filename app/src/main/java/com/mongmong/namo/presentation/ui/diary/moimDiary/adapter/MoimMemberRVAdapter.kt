package com.mongmong.namo.presentation.ui.diary.moimDiary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemDiaryGroupMembersBinding
import com.mongmong.namo.domain.model.GroupUser

class MoimMemberRVAdapter(  // 그룹 다이어리 화면 멤버 이름
    private val members: List<GroupUser>
) :
    RecyclerView.Adapter<MoimMemberRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupMembersBinding = ItemDiaryGroupMembersBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemDiaryGroupMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(member: GroupUser) {
            binding.peopleNameTv.text = member.userName
        }
    }
}
