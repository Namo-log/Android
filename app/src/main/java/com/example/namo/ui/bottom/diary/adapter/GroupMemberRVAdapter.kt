package com.example.namo.ui.bottom.diary.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.ItemDiaryGroupPeopleBinding

class GroupMemberRVAdapter (
    private val members:List<GroupDiaryMember>
):
    RecyclerView.Adapter<GroupMemberRVAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupPeopleBinding = ItemDiaryGroupPeopleBinding.inflate(
            LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      holder.bind(members[position] )
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemDiaryGroupPeopleBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(member:GroupDiaryMember){
            binding.peopleNameTv.text=member.memberName
        }
    }
}
