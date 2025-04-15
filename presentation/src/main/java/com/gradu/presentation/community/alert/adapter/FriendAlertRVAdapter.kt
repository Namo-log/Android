package com.gradu.presentation.community.alert.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gradu.domain.model.FriendRequest
import com.gradu.presentation.databinding.ItemFriendAlertBinding

class FriendAlertRVAdapter: RecyclerView.Adapter<FriendAlertRVAdapter.ViewHolder>(){

    private var friendRequestList = emptyList<FriendRequest>()
    private lateinit var mItemClickListener: MyItemClickListener

    fun setItemClickListener(itemClickListener: MyItemClickListener) {
        mItemClickListener = itemClickListener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addRequest(friendList: List<FriendRequest>) {
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
        fun bind(request: FriendRequest) {
            binding.friend = request
        }
    }
}