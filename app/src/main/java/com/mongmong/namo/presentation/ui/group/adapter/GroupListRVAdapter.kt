package com.mongmong.namo.presentation.ui.group.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mongmong.namo.R
import com.mongmong.namo.domain.model.group.Group
import com.mongmong.namo.databinding.ItemGroupListBinding

class GroupListRVAdapter(private var groups: List<Group>):  RecyclerView.Adapter<GroupListRVAdapter.ViewHolder>() {

    private lateinit var context : Context

    interface ItemClickListener{
        fun onItemClick(moim : Group)
    }

    private lateinit var mItemClickListener : ItemClickListener

    fun setMyItemClickListener (itemClickListener : ItemClickListener){
        mItemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemGroupListBinding = ItemGroupListBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        context = viewGroup.context

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(groups[position])

        holder.itemView.setOnClickListener {
            mItemClickListener.onItemClick(groups[position])
        }
    }

    override fun getItemCount(): Int = groups.size

    fun updateGroups(newGroups: List<Group>) {
        this.groups = newGroups
        notifyDataSetChanged() // 데이터가 변경됨을 알리고 RecyclerView를 갱신
    }

    inner class ViewHolder(val binding: ItemGroupListBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(moim: Group) {
            binding.itemGroupTitleTv.text = moim.groupName

            Glide.with(context)
                .load(moim.groupImgUrl)
                .placeholder(R.color.notyetGray)
                .error(R.drawable.app_logo_namo)
                .fallback(R.drawable.app_logo_namo)
                .circleCrop()
                .into(binding.itemGroupCoverImgIv)

            binding.itemGroupTotalPeopleNumTv.text = moim.groupMembers.size.toString()

            val userNameList: List<String> = moim.groupMembers.map { it.userName }
            binding.itemGroupTotalPeopleNameTv.text = userNameList.joinToString()
        }
    }
}