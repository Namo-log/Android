package com.mongmong.namo.presentation.ui.community.moim.schedule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemFriendInviteBinding
import com.mongmong.namo.domain.model.Friend

class FriendInviteRVAdapter: RecyclerView.Adapter<FriendInviteRVAdapter.ViewHolder>(){

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
        fun onInviteButtonClick(position: Int)
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFriendInviteBinding = ItemFriendInviteBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendList[position])
        holder.apply {
            // 아이템 전체 클릭
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(position)
            }
            // 초대 버튼 클릭
            binding.itemFriendInviteBtn.setOnClickListener {
                mItemClickListener.onInviteButtonClick(position)
            }
        }
    }

    override fun getItemCount(): Int = friendList.size

    inner class ViewHolder(val binding: ItemFriendInviteBinding) : RecyclerView.ViewHolder(binding.root) {
        //TODO: 실제 친구 데이터로 변경
        fun bind(friend: Friend) {
            binding.friend = friend
        }
    }
}