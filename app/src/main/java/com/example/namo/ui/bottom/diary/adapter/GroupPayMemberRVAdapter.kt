package com.example.namo.ui.bottom.diary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.diary.GroupDiaryMember
import com.example.namo.databinding.ItemDiaryGroupPayMemberBinding

class GroupPayMemberRVAdapter (  // 그룹 장소별 정산 다이얼로그 멤버
    private val members:List<GroupDiaryMember>,private var memberIsChecked:ArrayList<Boolean>
):
    RecyclerView.Adapter<GroupPayMemberRVAdapter.ViewHolder>(){

    /** 체크한 멤버 정산 **/
    interface PeopleItemClickListener{
        fun onItemClick(peopleList : ArrayList<GroupDiaryMember>, memberIsChecked: ArrayList<Boolean>)
    }
    private lateinit var mItemClickListener : PeopleItemClickListener
    fun setPeopleItemClickListener(itemClickListener : PeopleItemClickListener){
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGroupPayMemberBinding = ItemDiaryGroupPayMemberBinding.inflate(
            LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(members[position], memberIsChecked[position])

        holder.binding.itemGroupPayMemberCheckBox.setOnClickListener {

            memberIsChecked[position] = holder.binding.itemGroupPayMemberCheckBox.isChecked
            mItemClickListener.onItemClick(members as ArrayList<GroupDiaryMember>, memberIsChecked)
        }
    }

    override fun getItemCount(): Int = members.size

    inner class ViewHolder(val binding:ItemDiaryGroupPayMemberBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(member: GroupDiaryMember,isChecked:Boolean){
            binding.itemGroupMemberName.text=member.memberName
            binding.itemGroupPayMemberCheckBox.isChecked=isChecked
        }
    }
}
