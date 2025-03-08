package com.mongmong.namo.presentation.ui.community.moim.schedule.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemFriendInviteBinding
import com.mongmong.namo.domain.model.Friend

class FriendInviteRVAdapter(
    val canEdit: Boolean? = true
): RecyclerView.Adapter<FriendInviteRVAdapter.ViewHolder>(){

    private var friendList = emptyList<Friend>()
    private var isFriendSelectedList = mutableListOf<Boolean>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addFriend(friendList: List<Friend>) {
        this.friendList = friendList
        this.isFriendSelectedList = MutableList(friendList.size) { false } // 친구 선택 여부 초기화
        notifyDataSetChanged()
    }

    // 초대 해제
    fun uninvitedFriend(friend: Friend) {
        val position = friendList.indexOf(friend)
        if (position != -1) {
            isFriendSelectedList[position] = false
            notifyItemChanged(position)
        }
    }

    // 전체 선택 취소
    fun resetAllSelectedFriend() {
        this.isFriendSelectedList = MutableList(friendList.size) { false }
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onInviteButtonClick(isSelected: Boolean, position: Int)
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
            binding.itemFriendInviteBtn.isChecked = isFriendSelectedList[position]

            // 아이템 전체 클릭
            itemView.setOnClickListener {
                mItemClickListener.onItemClick(position)
            }

            // 초대 버튼 클릭 (초대할 친구에 추가 or 해제)
            binding.itemFriendInviteBtn.setOnClickListener {
                isFriendSelectedList[position] = binding.itemFriendInviteBtn.isChecked
                mItemClickListener.onInviteButtonClick(isFriendSelectedList[position], position)
            }
        }
    }

    override fun getItemCount(): Int = friendList.size

    inner class ViewHolder(val binding: ItemFriendInviteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.friend = friend
            binding.canEdit = canEdit
        }
    }
}