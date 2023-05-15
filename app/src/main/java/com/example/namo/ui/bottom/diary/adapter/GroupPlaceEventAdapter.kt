package com.example.namo.ui.bottom.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.DiaryGroupEvent
import com.example.namo.databinding.ItemDiaryGroupEventBinding

class GroupPlaceEventAdapter (
    private val members:List<DiaryGroupEvent>
):
    RecyclerView.Adapter<GroupPlaceEventAdapter.ViewHolder>(){

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupEventBinding = ItemDiaryGroupEventBinding.inflate(
            LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        holder.bind(members[position] )
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding: ItemDiaryGroupEventBinding): RecyclerView.ViewHolder(binding.root){
//        fun bind(member: GroupDiaryMember){
//            binding.peopleNameTv.text=member.memberName
//        }
    }
}
