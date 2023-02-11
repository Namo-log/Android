package com.example.namo.ui.bottom.grouplist


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.data.entity.group.Group
import com.example.namo.databinding.ItemGroupListBinding

class GroupListRVAdapter(private val groupList: ArrayList<Group>):  RecyclerView.Adapter<GroupListRVAdapter.ViewHolder>() {

    interface ItemClickListener{
        fun onItemClick(group : Group)
    }

    private lateinit var mItemClickListener : ItemClickListener
    fun setMyItemClickListener (itemClickListener : ItemClickListener){
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGroupListBinding = ItemGroupListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(groupList[position])

        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(groupList[position])
        }
    }

    override fun getItemCount(): Int = groupList.size

    inner class ViewHolder(val binding: ItemGroupListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.itemGroupTitleTv.text = group.title
            binding.itemGroupCoverImgCiv.setImageResource(group.coverImage!!)
            binding.itemGroupTotalPeopleNumTv.text = group.memberNum.toString()
            binding.itemGroupTotalPeopleNameTv.text = group.member
        }
    }
}