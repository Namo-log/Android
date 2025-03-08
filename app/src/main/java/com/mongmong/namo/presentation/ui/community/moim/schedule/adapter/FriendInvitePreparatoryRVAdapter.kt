package com.mongmong.namo.presentation.ui.community.moim.schedule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemFriendToInviteBinding
import com.mongmong.namo.domain.model.Friend

class FriendInvitePreparatoryRVAdapter: RecyclerView.Adapter<FriendInvitePreparatoryRVAdapter.ViewHolder>(){

    private var friendList = emptyList<Friend>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addFriend(friendList: List<Friend>) {
        this.friendList = friendList
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onDeleteBtnClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFriendToInviteBinding = ItemFriendToInviteBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendList[position])
        holder.apply {
            // 삭제 버튼 클릭
            binding.imageDeleteBtn.setOnClickListener {
                mItemClickListener.onDeleteBtnClick(position)
            }
        }
    }

    override fun getItemCount(): Int = friendList.size

    inner class ViewHolder(val binding: ItemFriendToInviteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.friend = friend
        }
    }
}