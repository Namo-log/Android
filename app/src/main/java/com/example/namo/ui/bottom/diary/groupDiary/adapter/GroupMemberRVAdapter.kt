package com.example.namo.ui.bottom.diary.groupDiary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.databinding.ItemDiaryGroupMembersBinding

class GroupMemberRVAdapter (  // 그룹 다이어리 화면 멤버 이름
    private val members:List<String>
):
    RecyclerView.Adapter<GroupMemberRVAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupMembersBinding = ItemDiaryGroupMembersBinding.inflate(
            LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      holder.bind(members[position] )
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemDiaryGroupMembersBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(member:String){
            binding.peopleNameTv.text=member
        }
    }
}
