package com.mongmong.namo.presentation.ui.community.alert.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mongmong.namo.databinding.ItemFriendAlertBinding
import com.mongmong.namo.domain.model.Friend

class FriendAlertRVAdapter: RecyclerView.Adapter<FriendAlertRVAdapter.ViewHolder>(){

    private var friendRequestList = emptyList<Friend>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRequest(friendList: List<Friend>) {
        this.friendRequestList = friendList
        notifyDataSetChanged()
    }

    interface MyItemClickListener {
        fun onFriendInfoClick(position: Int)
        fun onAcceptBtnClick(position: Int)
        fun onDenyBtnClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFriendAlertBinding = ItemFriendAlertBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friendRequestList[position])
        holder.apply {
            // 친구 정보 확인
            binding.itemFriendAlertLl.setOnClickListener {
                mItemClickListener.onFriendInfoClick(position)
            }
            // 수락 버튼 클릭
            binding.itemFriendAlertAcceptBtn.setOnClickListener {
                mItemClickListener.onAcceptBtnClick(position)
            }
            // 거절 버튼 클릭
            binding.itemFriendAlertDenyBtn.setOnClickListener {
                mItemClickListener.onDenyBtnClick(position)
            }
        }
    }

    override fun getItemCount(): Int = friendRequestList.size

    inner class ViewHolder(val binding: ItemFriendAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        //TODO: 실제 친구 요청 데이터로 변경
        fun bind(friend: Friend) {
            binding.friend = friend
        }
    }
}